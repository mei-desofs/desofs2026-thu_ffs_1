package bioCanteenApp.users.mapper;

import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.dto.GetUserDTO;
import bioCanteenApp.users.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements IUserMapper {

    @Override
    public User toDomain(UserDTO dto) {
        return new User(dto.getEmail(), dto.getName(), dto.getPassword());
    }

    @Override
    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .password(user.getPassword())
                .role(user.getRole().name())
                .canteenId(user.getCanteen() != null ? user.getCanteen().getId() : null)
                .diningHallId(user.getDiningHall() != null ? user.getDiningHall().getId() : null)
                .build();
    }


    @Override
    public GetUserDTO toGetDTO(User user) {
        return new GetUserDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().toString(),
                user.getCanteen() != null ? user.getCanteen().getId() : null,
                user.getDiningHall() != null ? user.getDiningHall().getId() : null
        );
    }
}
