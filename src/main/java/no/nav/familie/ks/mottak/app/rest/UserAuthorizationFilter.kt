package no.nav.familie.ks.mottak.app.rest

import no.nav.familie.sikkerhet.OIDCUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class UserAuthorizationFilter(@Value("\${MOTTAK_ROLLE:group1}") val påkrevdRolle: String, private val oidcUtil: OIDCUtil): OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        when {
            ourIssuer() == null -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No value for `ourIssuer`")
            currentUserGroups() == null -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No user-groups in JWT")
            !currentUserGroups().contains(påkrevdRolle) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing group $påkrevdRolle in JWT")
            else -> filterChain.doFilter(request,response)
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI.substring(request.contextPath.length)
        return !path.startsWith("/api/soknadmedvedlegg")
    }

    private fun ourIssuer () = oidcUtil.claimSet()
    private fun currentUserGroups() = ourIssuer().getAsList("groups")
}
