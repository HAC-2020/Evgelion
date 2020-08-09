package com.explainme
import android.app.Activity
import android.content.Context
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.lecture_card.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import kotlin.concurrent.thread

class LectureAdapter(private val lectures: ArrayList<Lecture>, private val context: Activity) :
    RecyclerView.Adapter<LectureAdapter.LectureHolder>() {

    class LectureHolder(val cardView: CardView, var expanded: Boolean = false) : RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): LectureHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.lecture_card, parent, false) as CardView
        val holder = LectureHolder(cardView)
        cardView.setOnClickListener {
            TransitionManager.beginDelayedTransition(it as CardView)
            if (!holder.expanded) {
                cardView.description.visibility = View.VISIBLE
                cardView.time_left.visibility = View.VISIBLE
            } else {
                cardView.description.visibility = View.GONE
                cardView.time_left.visibility = View.GONE
            }
            holder.expanded = !holder.expanded
        }
        return holder
    }

    override fun onBindViewHolder(holder: LectureHolder, position: Int) {
        holder.cardView.title_field.text = lectures[position].title
        holder.cardView.description.text = lectures[position].description
        holder.cardView.time_left.text = "%.2f".format((lectures[position].time - System.currentTimeMillis() / 1000) / 3600.0)
        thread{
            val user = get_user(lectures[position].author)
            context.runOnUiThread {
                holder.cardView.author_field.text = user.display_name
                if (user.photo_url != "null") {
                    Picasso.with(context).load(user.photo_url).into(holder.cardView.user_photo)
                }
            }
        }
    }

    override fun getItemCount() = lectures.size

    fun addLectures(newLectures: Array<Lecture>) {
        lectures.addAll(newLectures)
        notifyItemRangeInserted(itemCount - newLectures.size + 1, itemCount)
    }

    private fun get_user(id: String): User {
        val url = URL("http://192.168.1.6:8000")
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "POST"
        con.setRequestProperty("Content-Type", "application/json; utf-8")
        con.setRequestProperty("Accept", "application/json")
        con.doOutput = true
        val jsonObject = JSONObject()
        jsonObject.put("type", "get_user_by_id")
        jsonObject.put("google_id", id)
        con.outputStream.use { os ->
            val input: ByteArray = jsonObject.toString().toByteArray()
            os.write(input, 0, input.size)
        }
        var result: JSONObject? = null
        BufferedReader(
            InputStreamReader(con.inputStream, "utf-8")
        ).use { br ->
            val response = StringBuilder()
            var responseLine: String?
            while (br.readLine().also { responseLine = it } != null) {
                response.append(responseLine!!.trim { it <= ' ' })
            }
            val str = response.toString()
            result = if (str[str.length - 1] != '}') {
                Log.i("ExplainMe", "KEK!$str")
                JSONObject("$str}")
            } else {
                Log.i("ExplainMe", str)
                JSONObject(str)
            }
        }
        return User(
            id,
            result!!.getString("google_mail"),
            result!!.getString("photo_url"),
            result!!.getString("display_name")
        )
    }
}