package it.bollofacile

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityScenarioRule
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddVeicoloFlowTest {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun addVeicoloFlow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Click FAB to add vehicle
        onView(withId(androidx.compose.ui.test.R.id.fab_add_vehicle))
            .perform(click())
        
        // Fill form
        onView(withHint("Marca"))
            .perform(typeText("Fiat"))
        onView(withHint("Modello"))
            .perform(typeText("500"))
        onView(withHint("kW"))
            .perform(typeText("75"))
        onView(withHint("CV"))
            .perform(typeText("102"))
        
        // Select Euro class
        onView(withText("Classe Euro"))
            .perform(click())
        onView(withText("Euro 6"))
            .perform(click())
        
        // Select fuel type
        onView(withText("Alimentazione"))
            .perform(click())
        onView(withText("Benzina"))
            .perform(click())
        
        // Select region
        onView(withText("Regione"))
            .perform(click())
        onView(withText("Lombardia"))
            .perform(click())
        
        // Save
        onView(withText("Salva"))
            .perform(click())
        
        // Verify vehicle appears in list
        onView(withText("Fiat 500"))
            .check(matches(isDisplayed()))
    }
}