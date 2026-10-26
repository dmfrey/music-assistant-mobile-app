package io.music_assistant.client.ui.compose.settings

import io.music_assistant.client.ui.compose.common.TvFocusFlow
import io.music_assistant.client.utils.SessionState

// Android TV: Compose's geometric focus search does not reliably move focus between siblings
// on this hardware, so the initial-config form declares explicit directional links per target
// via FocusProperties instead of trusting geometry. Both connection tabs stay in the graph and
// link sideways; UP from the first field lands on the right-hand tab.
fun buildConfigLinks(isDirect: Boolean, hasCrashLog: Boolean): Map<String, TvFocusFlow.Links> =
    if (isDirect) {
        buildMap {
            put("exitApp", TvFocusFlow.Links(down = "tabDirect"))
            put("tabDirect", TvFocusFlow.Links(up = "exitApp", right = "tabWebRTC", down = "host"))
            put("tabWebRTC", TvFocusFlow.Links(up = "exitApp", left = "tabDirect", down = "host"))
            put("host", TvFocusFlow.Links(up = "tabWebRTC", down = "port"))
            put("port", TvFocusFlow.Links(up = "host", down = "tls"))
            put("tls", TvFocusFlow.Links(up = "port", down = "connect"))
            put("connect", TvFocusFlow.Links(up = "tls", down = "history", right = "history"))
            put("history", TvFocusFlow.Links(up = "connect", left = "connect", down = "shareLogs"))
            put("shareLogs", TvFocusFlow.Links(up = "history"))
            if (hasCrashLog) {
                put("shareLogs", TvFocusFlow.Links(up = "history", down = "crashShare"))
                put("crashShare", TvFocusFlow.Links(up = "shareLogs", right = "crashDelete"))
                put("crashDelete", TvFocusFlow.Links(up = "shareLogs", left = "crashShare"))
            }
        }
    } else {
        buildMap {
            put("exitApp", TvFocusFlow.Links(down = "tabDirect"))
            put("tabDirect", TvFocusFlow.Links(up = "exitApp", right = "tabWebRTC", down = "remoteId"))
            put("tabWebRTC", TvFocusFlow.Links(up = "exitApp", left = "tabDirect", down = "remoteId"))
            put("remoteId", TvFocusFlow.Links(up = "tabWebRTC", down = "connect"))
            put("connect", TvFocusFlow.Links(up = "remoteId", down = "history", right = "scanQr"))
            put("scanQr", TvFocusFlow.Links(left = "connect", right = "history", down = "history"))
            put("history", TvFocusFlow.Links(up = "connect", left = "scanQr", down = "shareLogs"))
            put("shareLogs", TvFocusFlow.Links(up = "history"))
            if (hasCrashLog) {
                put("shareLogs", TvFocusFlow.Links(up = "history", down = "crashShare"))
                put("crashShare", TvFocusFlow.Links(up = "shareLogs", right = "crashDelete"))
                put("crashDelete", TvFocusFlow.Links(up = "shareLogs", left = "crashShare"))
            }
        }
    }

// Same explicit-link strategy for the connected screen: the Server/Login/Local-player cards are
// separate composables whose controls the geometric search on this hardware also fails to chain
// (verified: D-pad focus gets stuck on the Codec and Enable rows and can't move up). The chain
// follows the on-screen order and adapts to whichever controls actually exist right now (the
// login form vs. the logged-in row; the Sendspin custom-connection fields vs. the plain toggle).
// Local-player fields stay in the chain regardless of [SendspinPlayerSettings] enablement: the
// draft/save/reset flow (SendspinSettingsViewModel) keeps them editable even while the player runs.
fun buildAuthLinks(
    sessionState: SessionState,
    isAuthenticated: Boolean,
    sendspinCustomConnectionEnabled: Boolean,
    hasCrashLog: Boolean,
): Map<String, TvFocusFlow.Links> = when {
    sessionState is SessionState.Connected && !isAuthenticated -> buildMap {
        put("exitApp", TvFocusFlow.Links(down = "disconnect"))
        put("disconnect", TvFocusFlow.Links(up = "exitApp", down = "loginTab"))
        put("loginTab", TvFocusFlow.Links(up = "disconnect", down = "username"))
        put("username", TvFocusFlow.Links(up = "loginTab", down = "password"))
        put("password", TvFocusFlow.Links(up = "username", down = "login", right = "passwordToggle"))
        put("passwordToggle", TvFocusFlow.Links(up = "password", down = "login", left = "password"))
        put("login", TvFocusFlow.Links(up = "password", down = "shareLogs"))
        put("shareLogs", TvFocusFlow.Links(up = "login"))
        if (hasCrashLog) {
            put("shareLogs", TvFocusFlow.Links(up = "login", down = "crashShare"))
            put("crashShare", TvFocusFlow.Links(up = "shareLogs", right = "crashDelete"))
            put("crashDelete", TvFocusFlow.Links(up = "shareLogs", left = "crashShare"))
        }
    }

    sessionState is SessionState.Connected && sendspinCustomConnectionEnabled -> buildMap {
        put("disconnect", TvFocusFlow.Links(down = "logout"))
        put("logout", TvFocusFlow.Links(up = "disconnect", down = "playerToggle"))
        put("playerToggle", TvFocusFlow.Links(up = "logout", down = "playerName"))
        put("playerName", TvFocusFlow.Links(up = "playerToggle", down = "codec"))
        put("codec", TvFocusFlow.Links(up = "playerName", down = "hiRes"))
        put("hiRes", TvFocusFlow.Links(up = "codec", down = "customToggle"))
        put("customToggle", TvFocusFlow.Links(up = "hiRes", down = "customHost"))
        put("customHost", TvFocusFlow.Links(up = "customToggle", down = "customPort"))
        put("customPort", TvFocusFlow.Links(up = "customHost", right = "customPath", down = "customTls"))
        put("customPath", TvFocusFlow.Links(up = "customHost", left = "customPort", down = "customTls"))
        put("customTls", TvFocusFlow.Links(up = "customPort", down = "buffer"))
        put("buffer", TvFocusFlow.Links(up = "customTls", down = "reset"))
        put("reset", TvFocusFlow.Links(up = "buffer", right = "save", down = "shareLogs"))
        put("save", TvFocusFlow.Links(up = "buffer", left = "reset", down = "shareLogs"))
        put("shareLogs", TvFocusFlow.Links(up = "reset"))
        if (hasCrashLog) {
            put("shareLogs", TvFocusFlow.Links(up = "reset", down = "crashShare"))
            put("crashShare", TvFocusFlow.Links(up = "shareLogs", right = "crashDelete"))
            put("crashDelete", TvFocusFlow.Links(up = "shareLogs", left = "crashShare"))
        }
    }

    sessionState is SessionState.Connected -> buildMap {
        put("disconnect", TvFocusFlow.Links(down = "logout"))
        put("logout", TvFocusFlow.Links(up = "disconnect", down = "playerToggle"))
        put("playerToggle", TvFocusFlow.Links(up = "logout", down = "playerName"))
        put("playerName", TvFocusFlow.Links(up = "playerToggle", down = "codec"))
        put("codec", TvFocusFlow.Links(up = "playerName", down = "hiRes"))
        put("hiRes", TvFocusFlow.Links(up = "codec", down = "customToggle"))
        put("customToggle", TvFocusFlow.Links(up = "hiRes", down = "buffer"))
        put("buffer", TvFocusFlow.Links(up = "customToggle", down = "reset"))
        put("reset", TvFocusFlow.Links(up = "buffer", right = "save", down = "shareLogs"))
        put("save", TvFocusFlow.Links(up = "buffer", left = "reset", down = "shareLogs"))
        put("shareLogs", TvFocusFlow.Links(up = "reset"))
        if (hasCrashLog) {
            put("shareLogs", TvFocusFlow.Links(up = "reset", down = "crashShare"))
            put("crashShare", TvFocusFlow.Links(up = "shareLogs", right = "crashDelete"))
            put("crashDelete", TvFocusFlow.Links(up = "shareLogs", left = "crashShare"))
        }
    }

    else -> emptyMap()
}
