package bioCanteenApp.users.service;

import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.dto.GetUserDTO;
import bioCanteenApp.users.dto.UserDTO;

import java.util.List;

public interface IUserService {
    List<GetUserDTO> getAllUsers();
    UserDTO createUser(UserDTO dto);
    void deleteUser(String id);

    GetUserDTO findUserById(Long id);

    GetUserDTO getUserByEmail(String email);

}
