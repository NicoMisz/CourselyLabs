package com.courselylabs.courselylab.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.UserCreateDTO;
import com.courselylabs.courselylab.dto.UserDTO;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.mapper.UserMapper;
import com.courselylabs.courselylab.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        return userMapper.toDTOList(userRepository.findAll());
    }

    @Transactional(readOnly = true)
    public UserDTO findById(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public UserDTO findByEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findByRole(String role) {
        return userMapper.toDTOList(userRepository.findByRole(role));
    }

    public UserDTO create(UserCreateDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("User with email '" + dto.getEmail() + "' already exists");
        }

        UserEntity entity = userMapper.toEntity(dto);
        entity.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        entity.setIsVerified(false);
        entity.setIsActive(true);

        entity = userRepository.save(entity);
        return userMapper.toDTO(entity);
    }

    public UserDTO update(UUID id, UserDTO dto) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userMapper.updateEntityFromDTO(dto, entity);
        entity = userRepository.save(entity);
        return userMapper.toDTO(entity);
    }

    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    public UserDTO deactivate(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        entity.setIsActive(false);
        entity = userRepository.save(entity);
        return userMapper.toDTO(entity);
    }

    public UserDTO verify(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        entity.setIsVerified(true);
        entity = userRepository.save(entity);
        return userMapper.toDTO(entity);
    }
}
