package com.dranikpg

inline fun<R> loop(action: () -> R) {
    while(true) {
        action()
    }
}
