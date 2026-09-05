package com.example.data.model

enum class CardType(val displayName: String) {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    ID_CARD("ID Card")
}

enum class IdCardType(
    val displayName: String,
    val defaultIssuer: String,
    val numberHint: String
) {
    AADHAAR("Aadhaar Card", "UIDAI", "12-digit UID (e.g. 1234 5678 9012)"),
    PAN("PAN Card", "Income Tax Department", "10-character PAN (e.g. ABCDE1234F)"),
    DRIVING_LICENSE("Driving Licence", "MoRTH / State RTO", "DL Number (e.g. DL-0420110012345)"),
    VOTER_ID("Voter ID / EPIC", "Election Commission of India", "10-character EPIC (e.g. ABC1234567)"),
    PASSPORT("Indian Passport", "Ministry of External Affairs", "8-character Passport (e.g. A1234567)"),
    VEHICLE_RC("Vehicle RC (Smart Card)", "Transport Department", "Registration (e.g. MH12AB1234)"),
    ABHA_HEALTH("ABHA Health ID / Ayushman", "National Health Authority", "14-digit ABHA (e.g. 12-3456-7890-1234)"),
    RATION_CARD("Ration Card", "Food & Civil Supplies", "Ration Card Number"),
    OTHER("Other ID Card", "Issuing Authority", "Document Number")
}

enum class CardNetwork(val displayName: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    AMEX("American Express"),
    DISCOVER("Discover"),
    RUPAY("RuPay"),
    JCB("JCB"),
    UNIONPAY("UnionPay"),
    DINERS_CLUB("Diners Club"),
    OTHER("Card");

    companion object {
        fun detectNetwork(cardNumber: String): CardNetwork {
            val clean = cardNumber.replace(" ", "").replace("-", "")
            return when {
                clean.startsWith("4") -> VISA
                clean.startsWith("51") || clean.startsWith("52") || clean.startsWith("53") ||
                        clean.startsWith("54") || clean.startsWith("55") ||
                        (clean.length >= 4 && clean.substring(0, 4).toIntOrNull() in 2221..2720) -> MASTERCARD
                clean.startsWith("34") || clean.startsWith("37") -> AMEX
                clean.startsWith("6011") || clean.startsWith("65") ||
                        (clean.length >= 3 && clean.substring(0, 3).toIntOrNull() in 644..649) -> DISCOVER
                clean.startsWith("60") || clean.startsWith("6521") || clean.startsWith("6522") ||
                        clean.startsWith("81") || clean.startsWith("82") || clean.startsWith("508") -> RUPAY
                clean.startsWith("35") -> JCB
                clean.startsWith("62") -> UNIONPAY
                clean.startsWith("36") || clean.startsWith("38") || clean.startsWith("300") -> DINERS_CLUB
                else -> OTHER
            }
        }
    }
}
