package com.animedong.app.core.network

import com.google.gson.annotations.SerializedName

/**
 * Wrapper generik semua response backend sankavollerei:
 * { "status": "success", "statusCode": 200, "message": "", "ok": true, "data": {...} }
 */
data class ApiResponse<T>(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("statusCode") val statusCode: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: T? = null
)
