package bioCanteenApp.security.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VirusTotalService {

    @Value("${virustotal.api.key}")
    private String apiKey;

    public void scanFile(MultipartFile file) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // 1. Enviar o ficheiro para o VirusTotal
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-apikey", apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // O RestTemplate precisa do ficheiro como Resource para o enviar no POST
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() != null ? file.getOriginalFilename() : "file.pdf";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Faz o POST
        ResponseEntity<JsonNode> uploadResponse = restTemplate.postForEntity(
                "https://www.virustotal.com/api/v3/files", requestEntity, JsonNode.class);

        // Extrai o ID da análise gerado pelo VirusTotal
        String analysisId = uploadResponse.getBody().get("data").get("id").asText();

        // 2. Esperar pelo resultado (Polling)
        // O VirusTotal demora alguns segundos a analisar. Vamos verificar a cada 5 segundos.
        boolean finished = false;
        int maliciousVotes = 0;

        HttpHeaders getHeaders = new HttpHeaders();
        getHeaders.set("x-apikey", apiKey);
        HttpEntity<Void> getEntity = new HttpEntity<>(getHeaders);

        for (int i = 0; i < 24; i++) { // Tenta 6 vezes (30 segundos no máximo)
            Thread.sleep(5000); // Espera 5 segundos entre cada pergunta

            ResponseEntity<JsonNode> reportResponse = restTemplate.exchange(
                    "https://www.virustotal.com/api/v3/analyses/" + analysisId,
                    HttpMethod.GET, getEntity, JsonNode.class);

            String status = reportResponse.getBody().get("data").get("attributes").get("status").asText();

            if ("completed".equals(status)) {
                // Lê quantos motores de anti-vírus detetaram malware
                maliciousVotes = reportResponse.getBody().get("data").get("attributes")
                        .get("stats").get("malicious").asInt();
                finished = true;
                break;
            }
        }

        if (!finished) {
            // Se o VirusTotal estiver super lento, lançamos erro de timeout (ou podes deixar passar, tu decides)
            throw new RuntimeException("VirusTotal scan timed out. Please try again later.");
        }

        if (maliciousVotes > 0) {
            // BLOQUEIA A APLICAÇÃO SE TIVER VÍRUS!
            throw new IllegalArgumentException("SECURITY ALERT: VirusTotal detected a virus in the uploaded certificate!");
        }
    }
}
