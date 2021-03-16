package no.nav.familie.ks.mottak.util


import no.nav.familie.ks.mottak.app.util.TaskUtil.nesteTriggertidEksluderHelg
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class TaskUtilTest {

    @Test
    fun `hvis datetime er ukedag så blir neste triggertid datoen plus 15 minutter`() {
        assertThat(nesteTriggertidEksluderHelg(FREDAG)).isEqualTo(FREDAG)
        assertThat(nesteTriggertidEksluderHelg(MANDAG)).isEqualTo(MANDAG)
    }

    @Test
    fun `hvis datetime er helg så blir neste triggertid mandag kl 7`() {
        assertThat(nesteTriggertidEksluderHelg(LØRDAG)).isEqualTo(MANDAG)
        assertThat(nesteTriggertidEksluderHelg(SØNDAG)).isEqualTo(MANDAG)
    }

    companion object {
        private val FREDAG: LocalDateTime = LocalDateTime.of(2021,3, 12, 7, 0, 0)
        private val LØRDAG: LocalDateTime = LocalDateTime.of(2021, 3, 13, 7, 0, 0)
        private val SØNDAG: LocalDateTime = LocalDateTime.of(2021, 3, 14, 9, 0, 0)
        private val MANDAG: LocalDateTime = LocalDateTime.of(2021, 3, 15, 7, 0, 0)
    }

}

