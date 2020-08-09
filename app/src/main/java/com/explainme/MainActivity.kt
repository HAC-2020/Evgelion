package com.explainme
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    val LOADING_SIZE = 10
    var loading = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab.setOnClickListener { fab.isExpanded = !fab.isExpanded }
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val lm = LinearLayoutManager(this)
        val lectureAdapter = LectureAdapter(ArrayList(), this)
        recycler_view.apply {
            setHasFixedSize(false)
            layoutManager = lm
            adapter = lectureAdapter
            addOnScrollListener(object: OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visibleItemCount = lm.childCount
                    val totalItemCount = lm.itemCount
                    val pastVisibleItems = lm.findFirstVisibleItemPosition()
                    if (dy > 0 && !loading && visibleItemCount + pastVisibleItems >= totalItemCount) {
                        loading = true
                        thread {
                            val lectures = load_lectures(totalItemCount, totalItemCount + LOADING_SIZE)
                            runOnUiThread {
                                lectureAdapter.addLectures(lectures)
                                loading = false
                            }
                        }
                        Log.i("ExplainMe", "Loading...")
                    }
                }
            })
        }
        thread {
            val lectures = load_lectures(0, LOADING_SIZE)
            runOnUiThread {
                lectureAdapter.addLectures(lectures)
                loading = false
            }
        }
        create_lecture_button.setOnClickListener {
            val title_field = sheet.title_field
            Log.i("ExplainMe", title_field.text.toString())
            if (title_field.text.toString().isEmpty() || description_field.text.toString().isEmpty()) {
                Snackbar.make(recycler_view, "Please fill title field", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { p0, year, month, day ->
                    val mHour = c.get(Calendar.HOUR)
                    val mMinute = c.get(Calendar.MINUTE)
                    TimePickerDialog(this,
                        TimePickerDialog.OnTimeSetListener { p0, minute, hour -> //                            val date = GregorianCalendar(year, month, day, hour, minute)
                            //                            val date = GregorianCalendar(Calendar.getInstance().timeZone)
                            //                            date.set(year, month, day, hour, minute)
                            //                            Log.i("ExplainMe", date.timeZone.displayName)
                            //                            Log.i("ExplainMe", (date.timeInMillis / 1000).toString())
                            //                            date.timeZone = TimeZone.getTimeZone("UTC")
                            //                            Log.i("ExplainMe", date.timeZone.displayName)
                            //                            Log.i("ExplainMe", (date.timeInMillis / 1000).toString())
                            //                            val ldt = LocalDateTime.parse("$year-$month-${day}T$hour:$minute:00", ZoneI)
                            //                            val date = Time()
                            //                            date.set(0, minute, hour, day, month, year)
                            //                            val tz = TimeZone.getTimeZone("Asia/Yekaterinburg")
                            //                            val destFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            //                            destFormat.timeZone = tz
                            //                            dest
                            //                            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            //                            isoFormat.timeZone = TimeZone.getTimeZone("Asia/Yekaterinburg")
                            //                            val date = isoFormat.parse("${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}T${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00")!!

                            val time = System.currentTimeMillis() / 1000 + 1000
                            Log.i("ExplainMe", time.toString())
                            val title = title_field.text.toString()
                            val description = description_field.text.toString()
                            val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)!!
                            val user = account.email!!
                            Log.i("ExplainMe", "Creating new lecture")
                            title_field.text?.clear()
                            description_field.text?.clear()
                            thread {
                                register_lecture(Lecture(title, description, user, time.toInt(), ""))
                                runOnUiThread {
                                    fab.isExpanded = false
                                }
                            }
                        }, mHour, mMinute, false).show()
                }, mYear, mMonth, mDay).show()
        }
    }

    override fun onBackPressed() {
        if (fab.isExpanded) {
            fab.isExpanded = false
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        menuInflater.inflate(R.menu.mymenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.getItemId()
        if (id == R.id.mybutton) {
            signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun register_lecture(lecture: Lecture) {
        try {
            val url = URL("http://192.168.1.6:8000")
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("Content-Type", "application/json; utf-8")
            con.setRequestProperty("Accept", "application/json")
            con.doOutput = true
            val jsonObject = JSONObject()
            jsonObject.put("type", "add_lecture")
            jsonObject.put("title", lecture.title)
            jsonObject.put("description", lecture.description)
            jsonObject.put("author", lecture.author)
            jsonObject.put("time", lecture.time)
            Log.i("ExplainMe", jsonObject.toString())
            con.outputStream.use { os ->
                val input: ByteArray = jsonObject.toString().toByteArray()
                os.write(input, 0, input.size)
            }
            var jsonArray: JSONArray? = null
            BufferedReader(
                InputStreamReader(con.inputStream, "utf-8")
            ).use { br ->
                val response = StringBuilder()
                var responseLine: String?
                while (br.readLine().also { responseLine = it } != null) {
                    response.append(responseLine!!.trim { it <= ' ' })
                }
                val str = response.toString()
                Log.i("ExplainMe", str)
            }
        } catch (e: Exception) {}
    }

    fun load_lectures(begin: Int, end: Int): Array<Lecture> {
        val url = URL("http://192.168.1.6:8000")
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "POST"
        con.setRequestProperty("Content-Type", "application/json; utf-8")
        con.setRequestProperty("Accept", "application/json")
        con.doOutput = true
        val jsonObject = JSONObject()
        jsonObject.put("type", "get_lectures")
        jsonObject.put("begin", begin)
        jsonObject.put("end", end)
        con.outputStream.use { os ->
            val input: ByteArray = jsonObject.toString().toByteArray()
            os.write(input, 0, input.size)
        }
        var jsonArray: JSONArray? = null
        BufferedReader(
            InputStreamReader(con.inputStream, "utf-8")
        ).use { br ->
            val response = StringBuilder()
            var responseLine: String?
            while (br.readLine().also { responseLine = it } != null) {
                response.append(responseLine!!.trim { it <= ' ' })
            }
            val str = response.toString()
            if (str[str.length - 1] != ']') {
                Log.i("ExplainMe", "LOL:$str")
                jsonArray = JSONArray("$str]")
            } else {
                Log.i("ExplainMe", str)
                jsonArray = JSONArray(str)
            }
        }
        return (0 until jsonArray!!.length()).map {
            val json = jsonArray!!.get(it) as JSONObject
            Lecture(
                json.getString("title"),
                json.getString("description"),
                json.getString("author"),
                json.getInt("time"),
                json.getString("zoom_url")
            )
        }.toTypedArray()
    }

    private fun signOut() {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this, OnCompleteListener<Void?> {
                finish()
            })
    }

}