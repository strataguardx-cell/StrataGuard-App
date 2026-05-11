package com.strataguard.server.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FirebaseTokenFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization") ?: run {
            filterChain.doFilter(request, response)
            return
        }
        if (!authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val idToken = authHeader.removePrefix("Bearer ").trim()
        try {
            val decoded = FirebaseAuth.getInstance().verifyIdToken(idToken)
            val principal = FirebasePrincipal(uid = decoded.uid, email = decoded.email)
            val auth = UsernamePasswordAuthenticationToken(
                principal,
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER")),
            )
            SecurityContextHolder.getContext().authentication = auth
        } catch (e: FirebaseAuthException) {
            // Invalid token — leave SecurityContext empty (treated as anonymous)
        }

        filterChain.doFilter(request, response)
    }
}
