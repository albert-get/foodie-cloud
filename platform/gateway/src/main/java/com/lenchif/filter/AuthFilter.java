package com.lenchif.filter;

import com.lenchif.auth.service.AuthService;
import com.lenchif.auth.service.pojo.Account;
import com.lenchif.auth.service.pojo.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component("authFilter")
@Slf4j
public class AuthFilter implements GatewayFilter, Ordered {

    private static final String AUTH = "Authorization";
    private static final String USERNAME = "lenchif-user-name";
    private static final String USERID = "lenchif-user-id";

    @Autowired
    private AuthService authService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Auth start");
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders header = request.getHeaders();
        String token = header.getFirst(AUTH);
        String userId = header.getFirst(USERID);

        ServerHttpResponse response = exchange.getResponse();
        if (StringUtils.isBlank(token)) {
            log.error("token not found");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        Account acct = Account.builder().token(token).userId(userId).build();
        AuthResponse resp = authService.verify(acct);
        if (resp.getCode() != 1L) {
            log.error("invalid token");
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }

        // TODO ??????????????????????????????header????????????????????????
        ServerHttpRequest.Builder mutate = request.mutate();
        mutate.header(USERID, userId);
        mutate.header(AUTH, token);
        ServerHttpRequest buildReuqest = mutate.build();

        // TODO ????????????????????????????????????????????????response???header???
        response.getHeaders().add(USERID, userId);
        response.getHeaders().add(AUTH, token);
        return chain.filter(exchange.mutate()
                .request(buildReuqest)
                .response(response)
                .build());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
