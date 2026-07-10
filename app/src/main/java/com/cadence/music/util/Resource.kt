package com.cadence.music.util

/** Uniform success/error/loading wrapper so ViewModels never need to catch exceptions from repos. */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()

    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String) -> Unit): Resource<T> {
        if (this is Error) action(message)
        return this
    }
}

suspend fun <T> resourceOf(block: suspend () -> T): Resource<T> = try {
    Resource.Success(block())
} catch (t: Throwable) {
    Resource.Error(t.message ?: "Something went wrong", t)
}
