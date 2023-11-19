package com.renanlima.dolistspring.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.renanlima.dolistspring.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
@Component
public class TaskAuthFilter extends OncePerRequestFilter {
    @Autowired
    private IUserRepository userRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {

            var authorization = request.getHeader("Authorization");
            var authEncoded = authorization.substring("Basic".length()).trim();
            byte[] authDecode = Base64.getDecoder().decode(authEncoded);
            var authDecodeToString = new String(authDecode);
            String[] userCredentials = authDecodeToString.split(":");
            String userName = userCredentials[0];
            String userPassword = userCredentials[1];

            var user = this.userRepository.findByUserName(userName);

            if (user == null) {
                response.sendError(401, "User not found");

            } else {
                var verifyPassword = BCrypt.verifyer().verify(userPassword.toCharArray(), user.getPassword());

                if(verifyPassword.verified) {
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401, "Wrong Password");
                }
            }

        } else {
            filterChain.doFilter(request, response);
        }
    }
}
