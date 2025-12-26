package com.vkui.scripts

import com.vkui.scripts.theme.CamelCaseName
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createThemesRepository(): ThemesRepository {
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }
    return HttpThemesRepository(httpClient)
}

interface ThemesRepository {
    suspend fun getTheme(themeKey: ThemeKey): Theme
}

private class HttpThemesRepository(private val httpClient: HttpClient) : ThemesRepository {
    override suspend fun getTheme(themeKey: ThemeKey): Theme {
        val themeName = getThemeName(themeKey)
        val themeUrl = THEME_URL_FORMAT.format(themeName)
        val themeStruct = httpClient.get(themeUrl)
            .body<ThemeStruct>()
        return Theme(
            key = themeKey,
            name = themeName,
            struct = themeStruct
        )
    }

    private fun getThemeName(themeKey: ThemeKey): CamelCaseName {
        return when (themeKey) {
            ThemeKey.LIGHT -> CamelCaseName("vkontakteAndroid")
            ThemeKey.DARK -> CamelCaseName("vkontakteAndroidDark")
        }
    }

    companion object {
        private const val THEME_URL_FORMAT =
            "https://cdn.jsdelivr.net/npm/@vkontakte/vkui-tokens@latest/themes/%s/struct.json"
    }
}