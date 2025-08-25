package com.example.myapplication.android.ui.core.repo

import com.example.myapplication.android.ui.core.model.Company
import com.example.myapplication.android.ui.core.model.CompanyType
import com.example.myapplication.android.ui.core.model.Employee
import kotlinx.coroutines.flow.StateFlow

interface CompanyRepository {
    val currentCompany: StateFlow<Company?>
    val employees: StateFlow<List<Employee>>
    suspend fun createCompany(name: String, type: CompanyType, address: String?): Result<Company>
    suspend fun joinCompanyByCode(userId: String): Result<Company>
    suspend fun addEmployeeByUserId(userId: String): Result<Unit>
    suspend fun updateEmployee(employee: Employee): Result<Unit>
    suspend fun removeEmployee(userId: String): Result<Unit>
}