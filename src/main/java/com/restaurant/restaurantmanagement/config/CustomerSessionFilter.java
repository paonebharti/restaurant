package com.restaurant.restaurantmanagement.config;

import com.restaurant.restaurantmanagement.entity.Customer;
import com.restaurant.restaurantmanagement.repository.CustomerRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerSessionFilter extends OncePerRequestFilter {

    private final CustomerRepository customerRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String sessionToken = request.getHeader("X-Session-Token");

        if (sessionToken != null) {
            Optional<Customer> customer = customerRepository
                    .findBySessionToken(sessionToken);

            if (customer.isPresent()) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                customer.get(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
