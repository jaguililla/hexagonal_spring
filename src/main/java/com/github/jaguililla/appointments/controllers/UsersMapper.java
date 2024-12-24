package com.github.jaguililla.appointments.controllers;

import com.github.jaguililla.appointments.domain.model.User;
import com.github.jaguililla.appointments.http.controllers.messages.UserRequest;
import com.github.jaguililla.appointments.http.controllers.messages.UserResponse;

public interface UsersMapper {

    static User user(UserRequest userRequest) {
        final var id = userRequest.getId();
        final var name = userRequest.getName();
        return new User(id, name);
    }

    static UserResponse userResponse(User user) {
        return new UserResponse().id(user.id()).name(user.name());
    }
}
