package com.example.financemanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
// Импорт NavHostFragment для получения NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.financemanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Инициализация ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Получение NavController
        // Использование supportFragmentManager.findFragmentById для получения NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)

        // Проверка и приведение типа: КРИТИЧЕСКИ ВАЖНО для избежания краша
        if (navHostFragment is NavHostFragment) {
            val navController = navHostFragment.navController

            // 3. Привязка BottomNavigationView к NavController
            // ID bottomNavView генерируется из bottom_nav_view в XML
            binding.bottomNavView.setupWithNavController(navController)
        } else {
            // Если NavHostFragment не найден или имеет неверный тип,
            // можно вывести лог ошибки или показать Toast
            // Log.e("MainActivity", "NavHostFragment not found or is of wrong type.")
        }
    }
}