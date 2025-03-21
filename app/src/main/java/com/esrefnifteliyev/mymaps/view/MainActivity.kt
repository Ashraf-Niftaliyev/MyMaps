 package com.esrefnifteliyev.mymaps.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.esrefnifteliyev.mymaps.R
import com.esrefnifteliyev.mymaps.databinding.ActivityMainBinding

 class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

     override fun onCreateOptionsMenu(menu: Menu?): Boolean {
         val menuInflater = menuInflater
         menuInflater.inflate(R.menu.place_menu,menu)
         return super.onCreateOptionsMenu(menu)
     }

     override fun onOptionsItemSelected(item: MenuItem): Boolean {
         if (item.itemId == R.id.add_place){
             val intent = Intent(this@MainActivity, MapsActivity::class.java)
             startActivity(intent)
         }
         return super.onOptionsItemSelected(item)
     }

}