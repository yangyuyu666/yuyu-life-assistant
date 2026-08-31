package com.yuyulife.assistant.domain.model

object LedgerCategories {
    val expense = listOf("餐饮", "交通", "购物", "居住", "娱乐", "其他")
    val income = listOf("工资", "奖金", "退款", "其他")

    fun forType(type: TransactionType): List<String> = when (type) {
        TransactionType.EXPENSE -> expense
        TransactionType.INCOME -> income
    }
}

