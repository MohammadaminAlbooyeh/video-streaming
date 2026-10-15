package it.bollofacile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddVeicoloFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreen_opensAddVeicoloForm() {
        // Resolve strings through the activity so the test follows the app's
        // per-app locale (Italian or English) instead of hardcoding one language.
        val addVehicle = composeRule.activity.getString(R.string.add_vehicle)
        val brand = composeRule.activity.getString(R.string.brand)
        val model = composeRule.activity.getString(R.string.model)

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(addVehicle).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(addVehicle).performClick()

        composeRule.onNodeWithText(brand).assertIsDisplayed()
        composeRule.onNodeWithText(model).assertIsDisplayed()

        composeRule.onNodeWithText(brand).performTextInput("Fiat")
        composeRule.onNodeWithText(model).performTextInput("500")
    }
}
