package it.bollofacile.ads

import androidx.compose.runtime.compositionLocalOf

/**
 * Whether the user's GDPR/UMP consent decision allows ad requests. Provided by
 * MainActivity once [ConsentManager.requestConsentAndShowFormIfRequired] completes;
 * false (the safe default) until then, so no composable requests an ad before consent
 * is resolved.
 */
val LocalAdsAllowed = compositionLocalOf { false }
