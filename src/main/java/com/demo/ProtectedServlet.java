package com.demo;

import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.ClaimsDefinition;
import jakarta.security.enterprise.identitystore.openid.OpenIdContext;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@OpenIdAuthenticationMechanismDefinition(
    clientId = "",
    clientSecret = "",
    redirectURI = "${baseURL}/callback",
    providerURI = "https://<uri>/",
    jwksConnectTimeout = 5000,
    jwksReadTimeout = 5000,
    extraParameters = {"audience=https://dev-zpn5pkmxf3rq70py.us.auth0.com/api/v2/"}, // <-- YOUR AUTH0 DOMAIN HERE
    claimsDefinition = @ClaimsDefinition(callerGroupsClaim = "http://www.jakartaee.demo/roles")
)
@WebServlet("/protected")
@ServletSecurity(
    @HttpConstraint(rolesAllowed = "Everyone")
)
public class ProtectedServlet extends HttpServlet {

    @Inject
    private OpenIdContext context;

    @Inject
    SecurityContext securityContext;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        var principal = securityContext.getCallerPrincipal();
        var name = principal.getName();

        String html = """
            <div style="margin: 0 10%%; width: 80%%; overflow-wrap: anywhere;">
                <h1>Protected Servlet</h1>
                <p>principal name: %s </p>
                <p>access token (type = %s):</p>
                <p>%s</p>
                <p>preferred_username: %s</p>
                <p>roles: %s</p>
                <p>claims:</p>
                <p>%s</p>
            </div>
            """.formatted(
            name,
            context.getTokenType(),
            context.getAccessToken(),
            context.getClaimsJson().get("preferred_username").toString(),
            context.getClaimsJson().get("http://www.jakartaee.demo/roles").toString(),
            context.getClaimsJson()
        );

        response.setContentType("text/html");
        response.getWriter().print(html.toString());
    }
}
