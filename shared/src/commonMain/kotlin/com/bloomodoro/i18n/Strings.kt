package com.bloomodoro.i18n

import androidx.compose.runtime.compositionLocalOf
import com.bloomodoro.model.AppLanguage
import com.bloomodoro.model.AppMode
import com.bloomodoro.model.NotifyMoment
import com.bloomodoro.model.Phase

/** Every static string the UI shows, in one place. Fully bilingual: Persian + English. */
data class AppStrings(
    val tabTimer: String,
    val tabGarden: String,
    val tabPlanning: String,
    val tabHistory: String,
    val tabPhrases: String,
    val tabTheme: String,
    val tabSettings: String,

    val modeStudy: String,
    val modeWork: String,

    val sessionsToday: (Int) -> String,
    val addToCalendarButton: String,
    val readyQuestion: String,
    val stopEarlyButton: String,
    val dailyTargetLabel: String,
    val dailyTargetSet: String,
    val setTodayTargetTitle: String,

    val sectionTiming: String,
    val focusDuration: String,
    val shortBreakDuration: String,
    val longBreakDuration: String,
    val sessionsBeforeLongBreak: String,
    val minutesUnit: String,
    val sectionMyNotifications: String,
    val notifyStart: String,
    val notifyMidpoint: String,
    val notifyEnd: String,
    val sectionStretch: String,
    val stretchEnabled: String,
    val stretchInterval: String,
    val sectionLiveFocus: String,
    val liveFocusEnabled: String,
    val editingSettingsFor: (String) -> String,

    val sectionLanguageRegion: String,
    val languageLabel: String,
    val languageFa: String,
    val languageEn: String,
    val calendarSystemLabel: String,
    val calendarJalali: String,
    val calendarGregorian: String,

    val colorPalette: String,
    val fullCustomization: String,
    val customizationHint: String,
    val backgroundColor: String,
    val primaryColor: String,
    val secondaryColor: String,
    val bloomAccentColor: String,
    val autoDarkMode: String,
    val companionSection: String,
    val companionNone: String,
    val companionBloom: String,
    val companionCat: String,
    val companionBlob: String,

    val phrasesHint: String,
    val noPhrasesYet: String,
    val addPhraseTitle: String,
    val phrasePlaceholder: String,
    val whenToShow: String,
    val save: String,
    val delete: String,
    val cancel: String,
    val filterAll: String,
    val notCompletedSuffix: String,
    val hoursMinutesText: (Int, Int) -> String,

    // Garden
    val gardenTitle: String,
    val gardenSubtitle: (Int) -> String,
    val gardenTodayLabel: String,
    val gardenEmpty: String,

    // History
    val historyTitle: String,
    val historyEmpty: String,
    val historyTotalLabel: (String) -> String,

    // Planning
    val planningTodoTab: String,
    val planningScheduleTab: String,
    val planningGoalsTab: String,
    val planningDeadlinesTab: String,
    val planningJourneyTab: String,
    val todoAddPlaceholder: String,
    val todoEmpty: String,
    val scheduleEmpty: String,
    val scheduleAddButton: String,
    val deadlinesEmpty: String,
    val deadlineAddButton: String,
    val goalsPyramidTitle: String,
    val goalsPyramidHint: String,
    val addYearGoal: String,
    val addStepUnder: String,

    // Goal Journey (free-form animated roadmap)
    val journeyTitle: String,
    val journeyHint: String,
    val addJourneyButton: String,
    val journeyEmpty: String,
    val addJourneyTitle: String,
    val journeyNamePlaceholder: String,
    val addStepButton: String,
    val stepNamePlaceholder: String,
    val journeyProgress: (Int, Int) -> String,
    val journeyAllDone: String,

    val phaseLabelFor: (Phase) -> String,
    val phaseTitleFor: (Phase) -> String,
    val momentLabelFor: (NotifyMoment) -> String,
    val modeLabelFor: (AppMode) -> String,
    /** Monday-first (ISO order), used for schedule day labels. */
    val weekdayFullNames: List<String>,

    // Navigation / header
    val backButton: String,
    val gardenLogoHint: String,
    val historyIconHint: String,

    // Duration picker shown right after picking Study/Work on the Timer screen
    val pickDurationTitle: String,
    val customDurationLabel: String,

    // Settings screen — consolidated, collapsible sections
    val sectionAppearance: String,
    val sectionPhrasesInSettings: String,
    val sectionBackup: String,
    val darkModeLabel: String,
    val cloudBackupHint: String,
    val localBackupTitle: String,
    val localBackupHint: String,
    val exportToClipboard: String,
    val importFromClipboard: String,
    val copiedToClipboard: String,
    val importedSuccessfully: String,
    val importFailed: String,
    val pasteHere: String,
)

private val faStrings = AppStrings(
    tabTimer = "تایمر",
    tabGarden = "باغچه",
    tabPlanning = "برنامه‌ریزی",
    tabHistory = "تاریخچه",
    tabPhrases = "جمله‌ها",
    tabTheme = "تم",
    tabSettings = "تنظیمات",

    modeStudy = "درس‌خوندن",
    modeWork = "کار",

    sessionsToday = { n -> "$n جلسه امروز 🌸" },
    addToCalendarButton = "اضافه به تقویم",
    readyQuestion = "آماده‌ای؟ ✨",
    stopEarlyButton = "توقف زودهنگام",
    dailyTargetLabel = "هدف امروز",
    dailyTargetSet = "تنظیم هدف امروز",
    setTodayTargetTitle = "امروز چند دقیقه می‌خوای درس/کار کنی؟",

    sectionTiming = "زمان‌بندی",
    focusDuration = "مدت تمرکز",
    shortBreakDuration = "استراحت کوتاه",
    longBreakDuration = "استراحت بلند",
    sessionsBeforeLongBreak = "تعداد جلسه قبل از استراحت بلند",
    minutesUnit = " دقیقه",
    sectionMyNotifications = "نوتیفیکیشن‌ها",
    notifyStart = "شروع هر جلسه",
    notifyMidpoint = "وسط هر جلسه",
    notifyEnd = "پایان هر جلسه",
    sectionStretch = "یادآور حرکت بدن",
    stretchEnabled = "یادآوری برای بلند شدن و کشش",
    stretchInterval = "هر چند دقیقه یک‌بار",
    sectionLiveFocus = "نمایش زنده روی لاک‌اسکرین",
    liveFocusEnabled = "نمایش تایمر در نوار اعلان/لاک‌اسکرین",
    editingSettingsFor = { mode -> "تنظیمات حالت: $mode" },

    sectionLanguageRegion = "زبان و تقویم",
    languageLabel = "زبان اپ",
    languageFa = "فارسی",
    languageEn = "English",
    calendarSystemLabel = "نوع تقویم",
    calendarJalali = "شمسی",
    calendarGregorian = "میلادی",

    colorPalette = "پالت رنگی",
    fullCustomization = "شخصی‌سازی کامل",
    customizationHint = "هر رنگ این پالت قابل تغییر و ذخیره است.",
    backgroundColor = "پس‌زمینه",
    primaryColor = "رنگ اصلی",
    secondaryColor = "رنگ فرعی",
    bloomAccentColor = "رنگ تاکیدی گل",
    autoDarkMode = "حالت تاریک خودکار (همراه سیستم)",
    companionSection = "همراه کوچولو",
    companionNone = "هیچکدام",
    companionBloom = "گل 🌸",
    companionCat = "بچه‌گربه 🐱",
    companionBlob = "بلاب 🟢",

    phrasesHint = "جمله‌های خودتو بنویس — یکیش موقع شروع، وسط یا پایان جلسه نشون داده می‌شه.",
    noPhrasesYet = "هنوز جمله‌ای نداری 🌱 با دکمه پایین اضافه کن",
    addPhraseTitle = "یه جمله جدید بنویس",
    phrasePlaceholder = "مثلاً: از پسش برمیای 💪",
    whenToShow = "کی نشون داده بشه؟",
    save = "ذخیره",
    delete = "حذف",
    cancel = "انصراف",
    filterAll = "همه",
    notCompletedSuffix = " (ناتمام)",
    hoursMinutesText = { h, m -> if (h > 0) "$h ساعت و $m دقیقه" else "$m دقیقه" },

    gardenTitle = "باغچه‌ی من",
    gardenSubtitle = { total -> "$total روز درس/کار خوندی، جنگلت داره بزرگ می‌شه 🌳" },
    gardenTodayLabel = "امروز",
    gardenEmpty = "هنوز هیچ گلی نکاشتی — اولین جلسه رو شروع کن 🌱",

    historyTitle = "تاریخچه‌ی کامل",
    historyEmpty = "هنوز جلسه‌ای ثبت نشده",
    historyTotalLabel = { total -> "مجموع: $total" },

    planningTodoTab = "کارها",
    planningScheduleTab = "برنامه",
    planningGoalsTab = "هدف‌ها",
    planningDeadlinesTab = "کلاس/ددلاین",
    planningJourneyTab = "مسیر هدف",
    todoAddPlaceholder = "یه کار جدید بنویس...",
    todoEmpty = "لیست کارها خالیه ✨",
    scheduleEmpty = "هنوز برنامه‌ای اضافه نکردی",
    scheduleAddButton = "افزودن به برنامه هفتگی",
    deadlinesEmpty = "کلاس یا ددلاینی ثبت نشده",
    deadlineAddButton = "افزودن کلاس/ددلاین",
    goalsPyramidTitle = "هرم هدف‌ها",
    goalsPyramidHint = "از بالا (هدف سالانه) به پایین (قدم‌های هفتگی) — هر پله رو بساز",
    addYearGoal = "افزودن هدف سالانه",
    addStepUnder = "افزودن قدم زیر این",

    journeyTitle = "مسیر رسیدن به هدف",
    journeyHint = "یه مقصد بنویس، بعد قدم‌به‌قدم چیزایی که باید انجام بدی رو اضافه کن — مثل «اپلای کردن» → زبان، مقاله، توصیه‌نامه...",
    addJourneyButton = "هدف جدید + مسیرش",
    journeyEmpty = "هنوز هیچ مسیری نساختی — با دکمه‌ی بالا شروع کن 🎯",
    addJourneyTitle = "این هدف رو کجا می‌خوای برسی؟",
    journeyNamePlaceholder = "مثلاً: اپلای کردن برای ارشد",
    addStepButton = "افزودن قدم",
    stepNamePlaceholder = "مثلاً: نمره زبان",
    journeyProgress = { done, total -> "$done از $total قدم" },
    journeyAllDone = "رسیدی! 🎉",

    phaseLabelFor = { phase ->
        when (phase) {
            Phase.WORK -> "تمرکز"
            Phase.SHORT_BREAK -> "استراحت کوتاه"
            Phase.LONG_BREAK -> "استراحت بلند"
            Phase.IDLE -> "—"
        }
    },
    phaseTitleFor = { phase ->
        when (phase) {
            Phase.WORK -> "وقت تمرکزه 🌱"
            Phase.SHORT_BREAK -> "استراحت کوتاه ☕"
            Phase.LONG_BREAK -> "استراحت بلند 🌿"
            Phase.IDLE -> "Bloomodoro"
        }
    },
    momentLabelFor = { moment ->
        when (moment) {
            NotifyMoment.START -> "شروع"
            NotifyMoment.MIDPOINT -> "وسط"
            NotifyMoment.END -> "پایان"
        }
    },
    modeLabelFor = { mode -> if (mode == AppMode.STUDY) "درس‌خوندن" else "کار" },
    weekdayFullNames = listOf("دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه", "یکشنبه"),

    backButton = "بازگشت",
    gardenLogoHint = "باغچه",
    historyIconHint = "تاریخچه",

    pickDurationTitle = "چقدر می‌خوای تمرکز کنی؟",
    customDurationLabel = "دلخواه",

    sectionAppearance = "تم و ظاهر",
    sectionPhrasesInSettings = "جمله‌های انگیزشی",
    sectionBackup = "بک‌آپ و همگام‌سازی",
    darkModeLabel = "حالت تاریک",
    cloudBackupHint = "برای کار کردن بک‌آپ ابری با ایمیل، باید یک پروژه‌ی Firebase واقعی وصل کنی (راهنما در docs/SETUP.md). تا اون‌موقع از پشتیبان‌گیری محلی زیر استفاده کن.",
    localBackupTitle = "پشتیبان‌گیری محلی (بدون نیاز به اینترنت)",
    localBackupHint = "یه نسخه از تمام اطلاعاتت (باغچه، تاریخچه، برنامه‌ریزی، تنظیمات) رو کپی کن و یه جای امن نگه‌دار. برای بازگردوندن، همون متن رو اینجا پیست کن.",
    exportToClipboard = "کپی پشتیبان در کلیپ‌بورد",
    importFromClipboard = "بازیابی از کلیپ‌بورد",
    copiedToClipboard = "کپی شد ✅",
    importedSuccessfully = "بازیابی شد ✅ — اپ رو ببند و دوباره باز کن",
    importFailed = "متن پیست‌شده معتبر نبود",
    pasteHere = "متن پشتیبان رو اینجا پیست کن",
)

private val enStrings = AppStrings(
    tabTimer = "Timer",
    tabGarden = "Garden",
    tabPlanning = "Planning",
    tabHistory = "History",
    tabPhrases = "Phrases",
    tabTheme = "Theme",
    tabSettings = "Settings",

    modeStudy = "Study",
    modeWork = "Work",

    sessionsToday = { n -> "$n sessions today 🌸" },
    addToCalendarButton = "Add to calendar",
    readyQuestion = "Ready? ✨",
    stopEarlyButton = "Stop early",
    dailyTargetLabel = "Today's target",
    dailyTargetSet = "Set today's target",
    setTodayTargetTitle = "How many minutes do you want to focus today?",

    sectionTiming = "Timing",
    focusDuration = "Focus duration",
    shortBreakDuration = "Short break",
    longBreakDuration = "Long break",
    sessionsBeforeLongBreak = "Sessions before a long break",
    minutesUnit = " min",
    sectionMyNotifications = "Notifications",
    notifyStart = "At the start of each session",
    notifyMidpoint = "At the midpoint",
    notifyEnd = "At the end",
    sectionStretch = "Stand up & stretch reminders",
    stretchEnabled = "Remind me to get up and stretch",
    stretchInterval = "Every N minutes",
    sectionLiveFocus = "Live lock-screen display",
    liveFocusEnabled = "Show the running timer in the notification/lock screen",
    editingSettingsFor = { mode -> "Editing settings for: $mode" },

    sectionLanguageRegion = "Language & calendar",
    languageLabel = "App language",
    languageFa = "فارسی",
    languageEn = "English",
    calendarSystemLabel = "Calendar system",
    calendarJalali = "Jalali (Shamsi)",
    calendarGregorian = "Gregorian",

    colorPalette = "Color palette",
    fullCustomization = "Full customization",
    customizationHint = "Every color in this palette is editable and saved.",
    backgroundColor = "Background",
    primaryColor = "Primary color",
    secondaryColor = "Secondary color",
    bloomAccentColor = "Bloom accent color",
    autoDarkMode = "Automatic dark mode (follow system)",
    companionSection = "Little companion",
    companionNone = "None",
    companionBloom = "Bloom 🌸",
    companionCat = "Kitten 🐱",
    companionBlob = "Blob 🟢",

    phrasesHint = "Write your own lines — one shows up at the start, middle, or end of a session.",
    noPhrasesYet = "No phrases yet 🌱 add one with the button below",
    addPhraseTitle = "Write a new phrase",
    phrasePlaceholder = "e.g. You've got this 💪",
    whenToShow = "When should it show?",
    save = "Save",
    delete = "Delete",
    cancel = "Cancel",
    filterAll = "All",
    notCompletedSuffix = " (incomplete)",
    hoursMinutesText = { h, m -> if (h > 0) "${h}h ${m}m" else "${m}m" },

    gardenTitle = "My Garden",
    gardenSubtitle = { total -> "$total days focused — your forest is growing 🌳" },
    gardenTodayLabel = "Today",
    gardenEmpty = "Nothing planted yet — start your first session 🌱",

    historyTitle = "Full History",
    historyEmpty = "No sessions logged yet",
    historyTotalLabel = { total -> "Total: $total" },

    planningTodoTab = "To-dos",
    planningScheduleTab = "Schedule",
    planningGoalsTab = "Goals",
    planningDeadlinesTab = "Classes/Deadlines",
    planningJourneyTab = "Goal Journey",
    todoAddPlaceholder = "Write a new to-do...",
    todoEmpty = "Your to-do list is empty ✨",
    scheduleEmpty = "No schedule blocks yet",
    scheduleAddButton = "Add to weekly schedule",
    deadlinesEmpty = "No classes or deadlines logged",
    deadlineAddButton = "Add class/deadline",
    goalsPyramidTitle = "Goal Pyramid",
    goalsPyramidHint = "From the top (yearly goal) down to weekly steps — build each level",
    addYearGoal = "Add a yearly goal",
    addStepUnder = "Add a step underneath",

    journeyTitle = "Path to your goal",
    journeyHint = "Write a destination, then add the concrete steps to get there — like \"Apply abroad\" → language exam, articles, recommendation letters...",
    addJourneyButton = "New goal + journey",
    journeyEmpty = "No journeys yet — start with the button above 🎯",
    addJourneyTitle = "What goal are you working toward?",
    journeyNamePlaceholder = "e.g. Apply for grad school",
    addStepButton = "Add step",
    stepNamePlaceholder = "e.g. Language exam score",
    journeyProgress = { done, total -> "$done of $total steps" },
    journeyAllDone = "You made it! 🎉",

    phaseLabelFor = { phase ->
        when (phase) {
            Phase.WORK -> "Focus"
            Phase.SHORT_BREAK -> "Short break"
            Phase.LONG_BREAK -> "Long break"
            Phase.IDLE -> "—"
        }
    },
    phaseTitleFor = { phase ->
        when (phase) {
            Phase.WORK -> "Time to focus 🌱"
            Phase.SHORT_BREAK -> "Short break ☕"
            Phase.LONG_BREAK -> "Long break 🌿"
            Phase.IDLE -> "Bloomodoro"
        }
    },
    momentLabelFor = { moment ->
        when (moment) {
            NotifyMoment.START -> "Start"
            NotifyMoment.MIDPOINT -> "Midpoint"
            NotifyMoment.END -> "End"
        }
    },
    modeLabelFor = { mode -> if (mode == AppMode.STUDY) "Study" else "Work" },
    weekdayFullNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),

    backButton = "Back",
    gardenLogoHint = "Garden",
    historyIconHint = "History",

    pickDurationTitle = "How long do you want to focus?",
    customDurationLabel = "Custom",

    sectionAppearance = "Theme & appearance",
    sectionPhrasesInSettings = "Motivational phrases",
    sectionBackup = "Backup & sync",
    darkModeLabel = "Dark mode",
    cloudBackupHint = "Cloud email backup needs your own real Firebase project connected (see docs/SETUP.md). Until then, use the local backup below.",
    localBackupTitle = "Local backup (no internet needed)",
    localBackupHint = "Copy a snapshot of everything (garden, history, planning, settings) somewhere safe. To restore, paste that same text back in here.",
    exportToClipboard = "Copy backup to clipboard",
    importFromClipboard = "Restore from clipboard",
    copiedToClipboard = "Copied ✅",
    importedSuccessfully = "Restored ✅ — close and reopen the app",
    importFailed = "That pasted text wasn't valid",
    pasteHere = "Paste your backup text here",
)

object Strings {
    val fa: AppStrings = faStrings
    val en: AppStrings = enStrings
    fun forLanguage(language: AppLanguage): AppStrings = if (language == AppLanguage.FA) fa else en
}

val LocalStrings = compositionLocalOf { Strings.fa }
