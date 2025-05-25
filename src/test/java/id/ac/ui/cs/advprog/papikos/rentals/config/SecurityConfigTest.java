//package id.ac.ui.cs.advprog.papikos.rentals.config;
//
//import id.ac.ui.cs.advprog.papikos.rentals.security.TokenAuthenticationFilter;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class SecurityConfigTest {
//
//    @Mock
//    private TokenAuthenticationFilter tokenAuthenticationFilter;
//
//    @Mock
//    private HttpSecurity httpSecurity;
//
//    @InjectMocks
//    private SecurityConfig securityConfig;
//
//    @Test
//    @SuppressWarnings("removal") // For HttpSecurity deprecated methods if any in future.
//    void testSecurityFilterChainConfiguration() throws Exception {
//        // Arrange
//        HttpSecurity.CsrfConfigurer csrfConfigurer = mock(HttpSecurity.CsrfConfigurer.class);
//        HttpSecurity.SessionManagementConfigurer sessionManagementConfigurer = mock(HttpSecurity.SessionManagementConfigurer.class);
//        HttpSecurity.AuthorizeHttpRequestsConfigurer authorizeHttpRequestsConfigurer = mock(HttpSecurity.AuthorizeHttpRequestsConfigurer.class);
//
//
//        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
//        when(csrfConfigurer.disable()).thenReturn(httpSecurity);
//        when(httpSecurity.csrf(any(AbstractHttpConfigurer.class))).thenAnswer(invocation -> {
//            AbstractHttpConfigurer<HttpSecurity.CsrfConfigurer, HttpSecurity> configurer = invocation.getArgument(0);
//            configurer.init(httpSecurity); // Simulate initialization
//            configurer.configure(httpSecurity); // Simulate configuration
//            return httpSecurity;
//        });
//
//
//        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
//        when(sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).thenReturn(sessionManagementConfigurer);
//        when(httpSecurity.sessionManagement(any(java.util.function.Customizer.class)))
//                .thenAnswer(invocation -> {
//                    java.util.function.Customizer<HttpSecurity.SessionManagementConfigurer<HttpSecurity>> customizer =
//                            invocation.getArgument(0);
//                    customizer.customize(sessionManagementConfigurer);
//                    return httpSecurity;
//                });
//
//
//        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
//        when(authorizeHttpRequestsConfigurer.requestMatchers(any(String.class), any(String.class))).thenReturn(authorizeHttpRequestsConfigurer);
//        when(authorizeHttpRequestsConfigurer.permitAll()).thenReturn(authorizeHttpRequestsConfigurer);
//        when(authorizeHttpRequestsConfigurer.anyRequest()).thenReturn(authorizeHttpRequestsConfigurer);
//        when(authorizeHttpRequestsConfigurer.authenticated()).thenReturn(authorizeHttpRequestsConfigurer);
//
//        when(httpSecurity.authorizeHttpRequests(any(java.util.function.Customizer.class)))
//                .thenAnswer(invocation -> {
//                    java.util.function.Customizer<HttpSecurity.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> customizer =
//                            invocation.getArgument(0);
//                    // Create a mock registry to pass to the customizer
//                    HttpSecurity.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry =
//                            mock(HttpSecurity.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);
//
//                    // Mock the behavior of the registry's methods that are used in SecurityConfig
//                    when(registry.requestMatchers(any(org.springframework.http.HttpMethod.class), any(String.class))).thenReturn(registry);
//                    when(registry.permitAll()).thenReturn(registry);
//                    when(registry.anyRequest()).thenReturn(registry);
//                    when(registry.authenticated()).thenReturn(registry);
//
//                    customizer.customize(registry);
//                    return httpSecurity;
//                });
//
//
//        when(httpSecurity.addFilterBefore(any(TokenAuthenticationFilter.class), eq(UsernamePasswordAuthenticationFilter.class)))
//                .thenReturn(httpSecurity);
//
//        SecurityFilterChain mockFilterChain = mock(SecurityFilterChain.class);
//        when(httpSecurity.build()).thenReturn(mockFilterChain);
//
//        // Act
//        SecurityFilterChain filterChain = securityConfig.securityFilterChain(httpSecurity);
//
//        // Assert
//        assertNotNull(filterChain);
//
//        verify(httpSecurity).csrf(any(AbstractHttpConfigurer.class));
//        verify(httpSecurity).sessionManagement(any(java.util.function.Customizer.class));
//        verify(sessionManagementConfigurer).sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//
//        // Verify authorizeHttpRequests
//        verify(httpSecurity).authorizeHttpRequests(any(java.util.function.Customizer.class));
//
//
//        verify(httpSecurity).addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//        verify(httpSecurity).build();
//    }
//}