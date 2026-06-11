package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isKeyAvailable(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotEmpty() && !key.contains("MY_GEMINI_API_KEY")
        } catch (e: Exception) {
            false
        }
    }

    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    suspend fun generateWealthInsights(
        isPersian: Boolean,
        totalNetWorthToman: Double,
        cashToman: Double,
        safeAssetsToman: Double,
        debtsToman: Double,
        receivablesToman: Double,
        inflationRate: Double = 45.0
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        val prompt = buildAnalysisPrompt(
            isPersian = isPersian,
            totalNetWorth = totalNetWorthToman,
            cash = cashToman,
            safeAssets = safeAssetsToman,
            debts = debtsToman,
            receivables = receivablesToman,
            inflationRate = inflationRate
        )

        if (isKeyAvailable()) {
            try {
                val systemPrompt = "You are a senior fintech analyst and economic advisory expert specializing in hyperinflation, currency devaluations, and asset preservation in emerging and high-inflation markets like Iran. Deliver a beautiful, readable personal advice report. Do not use markdown titles or complex bullet characters. Respond entirely in ${if (isPersian) "Persian (Farsi)" else "English"}."
                
                // Construct JSON request manually with org.json to guarantee 100% compile safety
                val requestBodyJson = JSONObject().apply {
                    put("contents", JSONArray().put(JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", prompt)
                        }))
                    }))
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", systemPrompt)
                        }))
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("maxOutputTokens", 800)
                    })
                }.toString()

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = requestBodyJson.toRequestBody(mediaType)

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBodyText = response.body?.string()
                        if (!responseBodyText.isNullOrBlank()) {
                            val candidatesObj = JSONObject(responseBodyText).getJSONArray("candidates")
                            val textVal = candidatesObj.getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text")
                            if (textVal.isNotBlank()) {
                                return@withContext textVal
                            }
                        }
                    } else {
                        Log.e(TAG, "Request failed with code: ${response.code} message: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API request failed, falling back to heuristics", e)
            }
        }

        return@withContext runRuleBasedInsights(
            isPersian = isPersian,
            totalNetWorth = totalNetWorthToman,
            cash = cashToman,
            safeAssets = safeAssetsToman,
            debts = debtsToman,
            receivables = receivablesToman,
            inflationRate = inflationRate
        )
    }

    private fun buildAnalysisPrompt(
        isPersian: Boolean,
        totalNetWorth: Double,
        cash: Double,
        safeAssets: Double,
        debts: Double,
        receivables: Double,
        inflationRate: Double
    ): String {
        return """
            Analyze the following personal finance portfolio under a $inflationRate% annual inflation rate:
            - Net Worth: $totalNetWorth Toman
            - Cash & Bank deposits (exposed to inflation): $cash Toman (${String.format("%.1f", (cash / totalNetWorth.coerceAtLeast(1.0)) * 100)}% of portfolio)
            - Hard Assets (Gold, Real Estate, Crypto - hedges against inflation): $safeAssets Toman (${String.format("%.1f", (safeAssets / totalNetWorth.coerceAtLeast(1.0)) * 100)}% of portfolio)
            - Receivables: $receivables Toman
            - Debts: $debts Toman

            Provide:
            1. An executive summary of their current wealth wellness.
            2. An exact breakdown of how inflation will eat their cash holdings (how much purchasing power is lost daily and annually).
            3. Detailed recommendations: Are they safe? Under what risk? What should they diversify?
            Language: ${if (isPersian) "Persian (Farsi زبان فارسی)" else "English"}.
        """.trimIndent()
    }

    private fun runRuleBasedInsights(
        isPersian: Boolean,
        totalNetWorth: Double,
        cash: Double,
        safeAssets: Double,
        debts: Double,
        receivables: Double,
        inflationRate: Double
    ): String {
        val cashRatio = (cash / totalNetWorth.coerceAtLeast(1.0)) * 100
        val safeRatio = (safeAssets / totalNetWorth.coerceAtLeast(1.0)) * 100
        val debtRatio = (debts / totalNetWorth.coerceAtLeast(1.0)) * 100

        val dailyLoss = (cash * (inflationRate / 100.0)) / 365.0
        val annualLoss = cash * (inflationRate / 100.0)

        if (isPersian) {
            val s = StringBuilder()
            s.append("💡 **گزارش خودکار سنجش دارایی لایو (مدل آفلاین)**\n\n")
            
            s.append("📊 **خلاصه وضعیت سبد دارایی:**\n")
            s.append("ارزش خالص کل دارایی‌های شما به طور تقریبی **${formatAmount(totalNetWorth)} تومان** ارزیابی شده است.\n")
            s.append("سهم سپرده‌ها و وجه نقد ریالی شما **${String.format("%.1f", cashRatio)}٪** از کل دارایی است. در اقتصادی با تورم بالا، وجه نقد فاقد پشتوانه (ریال/سپرده‌های بانکی بدون سود متناسب) سریع‌ترین کاهش ارزش را تجربه می‌کند.\n\n")

            s.append("⚠️ **برآورد آسیب زیان تورمی وجه نقد:**\n")
            s.append("با فرض نرخ تورم سالانه **$inflationRate٪**، ریال‌های نقدی شما با شتاب زیر در حال ذوب شدن هستند:\n")
            s.append("• زیان روزانه تخمینی قدرت خرید: **${formatAmount(dailyLoss)} تومان**\n")
            s.append("• زیان سالانه انباشته قدرت خرید: **${formatAmount(annualLoss)} تومان**\n\n")

            s.append("⚖️ **تحلیل ساختار ریسک:**\n")
            if (cashRatio > 25.0) {
                s.append("• 🔴 **ریسک بالا (بیش‌مواجه با ریال یا فیات):** شما بیش از ۲۵٪ دارایی خود را به صورت وجه نقد نگه داشته‌اید. در بازه‌های میان‌مدت، این وجه نقد ارزش خود را از دست می‌دهد. توصیه می‌شود بخشی را به دارایی‌های مقاوم تبدیل کنید.\n")
            } else {
                s.append("• 🟢 **سبد مقاوم:** هم‌پوشانی نقدی شما زیر ۲۵٪ است که نشان‌دهنده هوشمندی بالا در مصون‌سازی دارایی‌ها در برابر تورم.\n")
            }

            if (safeRatio > 60.0) {
                s.append("• 🟢 **سپر ایمن فعال:** بیش از ۶۰٪ دارایی‌های شما متکی بر طلا، املاک یا رمزارز است؛ این دارایی‌ها به طور تاریخی سپر پایداری در برابر کاهش ارزش ریال هستند.\n")
            } else {
                s.append("• 🟡 **سپر ایمن تضعیف شده:** سهم دارایی‌های ضد تورم شما پایین‌تر از ۵۰٪ است. توصیه می‌شود جهت ثبات بلند‌مدت، سهم طلا یا دارایی‌های مرجع را رشد دهید.\n")
            }

            if (debtRatio > 35.0) {
                s.append("• 🔴 **فشار بدهی:** بدهی‌های شما معادل بیش از ۳۵٪ از کل ثروت شماست. گرچه وام در اقتصاد تورمی ابزاری برای رشد است، اما دقت کنید نرخ بهره تجمعی آن نرخ رشد دارایی را کاهش ندهد.\n")
            } else {
                s.append("• 🟢 **تراز بدهی متوازن:** نرخ بدهی شما در امنیت کامل به سر می‌برد.\n")
            }

            s.append("\n💡 **پیشنهاد مشاور هوشمند:** از طلا (یک گرمی به بالا)، تتر یا سپرده‌های طلا به عنوان لنگرگاه نقدینگی خود استفاده کنید تا در مواقع ضرورت نقدشوندگی سریع داشته باشید و ارزش پولتان حفظ شود.")
            return s.toString()
        } else {
            val s = StringBuilder()
            s.append("💡 **Automatic Live Wealth Report (Offline Model)**\n\n")
            
            s.append("📊 **Portfolio Breakdown Summary:**\n")
            s.append("Your calculated net worth is **${formatAmount(totalNetWorth)} Toman**.\n")
            s.append("Cash allocation represents **${String.format("%.1f", cashRatio)}%** of your wealth. In a high-inflation environment, fiat cash and basic savings are highly exposed to losing purchasing power.\n\n")

            s.append("⚠️ **Inflation Damage Evaluation on Cash:**\n")
            s.append("Under a projected **$inflationRate%** annual inflation rate, your cash holdings suffer the following deterioration:\n")
            s.append("• Daily Estimated Purchasing Power Loss: **${formatAmount(dailyLoss)} Toman**\n")
            s.append("• Annual Cumulative Purchasing Power Loss: **${formatAmount(annualLoss)} Toman**\n\n")

            s.append("⚖️ **Risk Matrix Analysis:**\n")
            if (cashRatio > 25.0) {
                s.append("• 🔴 **High Fiat Exposure:** More than 25% of your total net worth is held in fiat deposits. In medium-term horizons, this money will melt away. Diversifying into hard assets is highly advised.\n")
            } else {
                s.append("• 🟢 **Healthy Composition:** Cash allocation is kept under 25%, establishing robust wealth defenses.\n")
            }

            if (safeRatio > 60.0) {
                s.append("• 🟢 **Solid Anti-Inflation Shield:** Above 60% of assets are locked in Gold, Real Estate, or Crypto, providing excellent defenses against currency devaluation.\n")
            } else {
                s.append("• 🟡 **Exposed Shield:** Hard assets are under 50%. Consider gradually swapping fiat cash for physical hedge instruments.\n")
            }

            if (debtRatio > 35.0) {
                s.append("• 🔴 **Debt Pressure:** Outgoing debts cover over 35% of total asset base. Exercise caution even if debt can sometimes serve as leverage in inflationary times.\n")
            } else {
                s.append("• 🟢 **Optimal Leverage:** Debt levels are highly conservative.\n")
            }

            s.append("\n💡 **Fintech Recommendation:** Maintain emergency funds in stable digital currencies like USDT or physical gold grams. This preserves transactional optionality while safeguarding real purchasing power.")
            return s.toString()
        }
    }

    private fun formatAmount(amount: Double): String {
        return if (amount >= 1_000_000_000) {
            String.format("%.2f B", amount / 1_000_000_000)
        } else if (amount >= 1_000_000) {
            String.format("%.2f M", amount / 1_000_000)
        } else {
            String.format("%,.0f", amount)
        }
    }
}
