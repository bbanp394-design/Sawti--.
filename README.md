# صوتي (Sawti) 📱
### تطبيق التواصل الميسر لكبار السن ولمن لا يستطيعون التحدث
### Accessibility Communication App for Elderly and Non-Speaking Individuals

**Sawti** is an Android application built with Kotlin and Jetpack Compose designed around the accessibility workflow:
**HANDWRITE (الكتابة باليد) → RECOGNIZE (التعرف الذكي) → CORRECT (التصحيح) → EDIT (التعديل) → SPEAK (النطق الصوتي)**

---

## 🌟 المميزات الرئيسية (Features)
- ✍️ **لوحة كتابة يدوية كبيرة (Handwriting Canvas):** مساحة واسعة ومريحة للكتابة بالأصبع أو القلم، تدعم العربية، الإنجليزية، والصينية.
- 🔄 **تعرف هجين ذكي (Hybrid Recognition):** محرك يتعرف على الأحرف والكلمات مع دعم العمل بدون إنترنت (Offline-first).
- 💡 **اقتراحات التصحيح الذكي (Smart Correction):** رصد وتصحيح الأخطاء الشائعة (مثل "مء" إلى "ماء").
- 🔊 **نطق صوتي فوري (Text-to-Speech):** أزرار ضخمة عالية التباين، تحكم بالسرعة مناسب لكبار السن ($0.85\times$)، ودعم النطق التلقائي.
- 💬 **عبارات سريعة قابلة للتخصيص (Quick Phrases):** قاعدة بيانات محلية (Room DB) تحتوي على الاحتياجات اليومية مع إمكانية إضافة وتعديل العبارات.
- 🆘 **وضع الطوارئ (Emergency Mode):** زر استغاثة واضح وشاشة تنبيهات طبية عاجلة.
- 👁️ **إمكانية وصول فائقة (Accessibility):** ألوان طبية عالية التباين (Medical Teal & Coral)، دعم الوضع الداكن، واهتزاز لمسي عند الضغط.

---

## 📥 طريقة تحميل وتثبيت التطبيق على الهاتف (How to Install)

### الطريقة 1: عبر GitHub Actions (تلقائي)
1. في هذا المستودع على GitHub، اضغط على تبويب **Actions**.
2. اختر آخر عملية بناء مكتملة باللون الأخضر (**Build Android APK**).
3. في أسفل الصفحة عند قسم **Artifacts**، اضغط على **`Sawti-Accessibility-App-Debug`** لتحميل ملف الـ APK وتثبيته على هاتفك مباشرة.

### الطريقة 2: عبر Android Studio
1. انسخ رابط هذا المستودع.
2. افتح **Android Studio** واختر **File → New → Project from Version Control...**.
3. اضغط زر **Run ▶** لتثبيته وتشغيله على أي هاتف متصل أو محاكي أندرويد.
