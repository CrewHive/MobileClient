package com.example.myapplication.android.ui.core.mappers

import com.example.myapplication.android.ui.state.CompanyEmployee

object ContractTypeMapper {
    /** API -> dominio */
    fun fromApi(api: String?): CompanyEmployee.ContractType? =
        api?.trim()?.uppercase()?.let { runCatching { CompanyEmployee.ContractType.valueOf(it) }.getOrNull() }

    /** dominio -> API */
    fun toApi(domain: CompanyEmployee.ContractType?): String? = domain?.name
}
