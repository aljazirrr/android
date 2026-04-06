package com.bariatric.assistant.features.tips

import com.bariatric.assistant.core.data.preferences.PostOpStage

data class Tip(
    val category: TipCategory,
    val title: String,
    val content: String,
    val applicableStages: List<PostOpStage> = PostOpStage.entries
)

enum class TipCategory(val displayName: String, val emoji: String) {
    HYDRATION("Hidratare", "💧"),
    PORTIONS("Porționare", "🥄"),
    VITAMINS("Vitamine", "💊"),
    EMOTIONAL("Emoțional", "💚"),
    ACTIVITY("Activitate", "🏃")
}

object TipsData {
    val allTips = listOf(
        // Hidratare
        Tip(
            TipCategory.HYDRATION,
            "Regula celor 30 de minute",
            "Nu bea lichide cu 30 de minute înainte și 30 de minute după masă. Lichidele în timpul mesei pot dilata stomacul și pot cauza disconfort."
        ),
        Tip(
            TipCategory.HYDRATION,
            "Înghițituri mici, dar frecvente",
            "Bea apă în înghițituri mici pe tot parcursul zilei, nu cantități mari odată. Poartă mereu o sticlă de apă cu tine."
        ),
        Tip(
            TipCategory.HYDRATION,
            "Lichide permise",
            "Apa, ceaiul fără zahăr, bulionul și apa cu felii de lămâie sau mentă sunt alegeri excelente. Evită băuturile carbogazoase și sucurile cu zahăr."
        ),
        Tip(
            TipCategory.HYDRATION,
            "Semnele deshidratării",
            "Urina închisă la culoare, oboseala, amețeala și buzele uscate sunt semne de deshidratare. Dacă le observi, crește imediat consumul de lichide."
        ),
        Tip(
            TipCategory.HYDRATION,
            "Obiectiv zilnic",
            "Încearcă să bei minim 1.5 litri de lichide pe zi. Pune alarme la fiecare oră ca reminder dacă uiți."
        ),

        // Porționare
        Tip(
            TipCategory.PORTIONS,
            "Proteinele primele!",
            "La fiecare masă, mănâncă mai întâi proteinele (carne, pește, ouă, iaurt), apoi legumele, și la final carbohidrații. Stomacul tău mic trebuie umplut cu ce e mai important.",
            listOf(PostOpStage.THREE_TO_SIX_MONTHS, PostOpStage.SIX_TO_TWELVE_MONTHS, PostOpStage.BEYOND_TWELVE)
        ),
        Tip(
            TipCategory.PORTIONS,
            "Mestecă de 20-30 de ori",
            "Fiecare îmbucătură trebuie mestecată de cel puțin 20-30 de ori, până devine o pastă uniformă. Acest lucru ajută digestia și previne disconfortul.",
        ),
        Tip(
            TipCategory.PORTIONS,
            "Farfurii mici, rezultate mari",
            "Folosește farfurii de desert în loc de farfurii normale. Vizual, porția va părea mai mare și te va ajuta psihologic."
        ),
        Tip(
            TipCategory.PORTIONS,
            "Mănâncă încet",
            "O masă ar trebui să dureze minim 20-30 de minute. Pune furculița jos între îmbucături. Corpul tău are nevoie de timp să trimită semnalul de sațietate."
        ),
        Tip(
            TipCategory.PORTIONS,
            "Oprește-te când ești sătul",
            "Învață să recunoști semnalele de sațietate: o presiune ușoară în stomac, un mic suspin, sau pierderea interesului pentru mâncare. NU mânca tot din farfurie doar pentru că e acolo."
        ),

        // Vitamine
        Tip(
            TipCategory.VITAMINS,
            "Suplimentele sunt esențiale",
            "După chirurgia bariatrică, corpul tău absoarbe mai puțini nutrienți. Suplimentele de vitamine și minerale sunt obligatorii pe viață, nu opționale."
        ),
        Tip(
            TipCategory.VITAMINS,
            "Vitamina B12",
            "Vitamina B12 este crucială pentru energie și funcționarea nervoasă. După bypass, absorbția este mult redusă. Discută cu medicul despre forma potrivită (sublingual, injecții)."
        ),
        Tip(
            TipCategory.VITAMINS,
            "Calciu și Vitamina D",
            "Calciul citrat (nu carbonat!) este forma recomandată după chirurgia bariatrică. Ia-l separat de fier, cu 2 ore distanță, împreună cu Vitamina D pentru absorbție."
        ),
        Tip(
            TipCategory.VITAMINS,
            "Fierul contează",
            "Deficiența de fier este frecventă, mai ales la femei. Ia fierul pe stomacul gol cu vitamina C (suc de lămâie) pentru absorbție maximă."
        ),
        Tip(
            TipCategory.VITAMINS,
            "Proteinele - macronutrientul cheie",
            "Obiectivul tău zilnic este de 60-80g proteine. Shake-urile proteice, pieptul de pui, peștele, ouăle și iaurtul grecesc sunt surse excelente."
        ),

        // Emoțional
        Tip(
            TipCategory.EMOTIONAL,
            "E un maraton, nu un sprint",
            "Progresul nu e mereu liniar. Vor fi zile bune și zile mai grele. Important e să nu renunți și să celebrezi fiecare mică victorie."
        ),
        Tip(
            TipCategory.EMOTIONAL,
            "Mâncatul emoțional",
            "Dacă simți nevoia să mănânci din emoție (stres, tristețe, plictiseală), oprește-te și întreabă-te: 'Chiar mi-e foame, sau simt altceva?' Scrie într-un jurnal ce simți."
        ),
        Tip(
            TipCategory.EMOTIONAL,
            "Compară-te doar cu tine",
            "Fiecare corp este diferit. Nu te compara cu alți pacienți bariatrici. Progresul tău este unic și valid."
        ),
        Tip(
            TipCategory.EMOTIONAL,
            "Cere ajutor",
            "Dacă te simți copleșit, anxios sau deprimat, nu ezita să discuți cu un psiholog specializat. Sănătatea mintală e la fel de importantă ca cea fizică."
        ),
        Tip(
            TipCategory.EMOTIONAL,
            "Celebrează victoriile non-scale",
            "Cântarul nu e singurul indicator de succes. Hainele care se potrivesc mai bine, energia crescută, somnul mai bun - toate astea contează enorm!"
        ),

        // Activitate
        Tip(
            TipCategory.ACTIVITY,
            "Mișcare zilnică",
            "Chiar și o plimbare de 15-20 minute pe zi face diferența enormă. Începe ușor și crește treptat."
        ),
        Tip(
            TipCategory.ACTIVITY,
            "Nu imediat după masă",
            "Așteaptă 30-60 de minute după masă înainte de activitate fizică intensă. O plimbare ușoară e în regulă.",
            listOf(PostOpStage.THREE_TO_SIX_MONTHS, PostOpStage.SIX_TO_TWELVE_MONTHS, PostOpStage.BEYOND_TWELVE)
        ),
        Tip(
            TipCategory.ACTIVITY,
            "Ascultă-ți corpul",
            "După operație, corpul tău se vindecă. Nu forța. Dacă simți durere sau amețeală, oprește-te. Recuperarea completă durează luni."
        ),
        Tip(
            TipCategory.ACTIVITY,
            "Construiește obiceiuri",
            "E mai important să faci mișcare 10 minute zilnic decât o oră o dată pe săptămână. Consecvența bate intensitatea.",
            listOf(PostOpStage.SIX_TO_TWELVE_MONTHS, PostOpStage.BEYOND_TWELVE)
        ),
        Tip(
            TipCategory.ACTIVITY,
            "Exerciții recomandate",
            "Mersul pe jos, înotul, yoga și ciclismul sunt excelente după chirurgia bariatrică. Evită exercițiile cu impact mare în primele luni.",
            listOf(PostOpStage.THREE_TO_SIX_MONTHS, PostOpStage.SIX_TO_TWELVE_MONTHS, PostOpStage.BEYOND_TWELVE)
        )
    )

    fun getTipsForStage(stage: PostOpStage?): List<Tip> {
        if (stage == null) return allTips
        return allTips.filter { it.applicableStages.contains(stage) }
    }

    fun getTipsByCategory(category: TipCategory, stage: PostOpStage? = null): List<Tip> {
        return getTipsForStage(stage).filter { it.category == category }
    }

    fun getRandomTip(stage: PostOpStage? = null): Tip {
        return getTipsForStage(stage).random()
    }
}
