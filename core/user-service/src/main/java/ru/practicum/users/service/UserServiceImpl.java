package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.UserDtoOut;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.exception.NotFoundException;
import ru.practicum.users.mapper.UserMapper;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDtoOut createUser(NewUserRequest request) {
        User user = UserMapper.toEntity(request);
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDtoOut getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDtoOut> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users;

        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAllWithOffset(pageable);
        } else {
            users = userRepository.findByIdIn(ids, pageable);
        }

        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public Map<Long, UserDtoOut> getUsers(Set<Long> ids) {
        List<User> users = userRepository.findAllById(ids);
        return users.stream().map(UserMapper::toDto).collect(Collectors.toMap(
                UserDtoOut::getId,
                Function.identity()
        ));
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
}