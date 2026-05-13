package bioCanteenApp.users.mapper;

import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.dto.GetUserDTO;
import bioCanteenApp.users.dto.UserDTO;

public interface IUserMapper {
    User toDomain(UserDTO dto);
    UserDTO toDTO(User user);
    GetUserDTO toGetDTO(User user);
}
