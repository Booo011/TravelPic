package com.example.travelpic.data

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.travelpic.getExifInfo
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.Context
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

//파이어베이스에 앨범에 사진 저장

@Composable
fun uploadImageToFirebase(uri: Uri, albumCode: String): String? {
    val storageReference = Firebase.storage.reference
    val databaseReference: DatabaseReference = Firebase.database.reference
    val context = LocalContext.current
    val imageReference = storageReference.child("images/${albumCode}/${uri.lastPathSegment}")
    return try {
        // Upload image to Firebase Storage
        imageReference.putFile(uri)

        // Get the download URL
        val downloadUrl = imageReference.downloadUrl.toString().toUri()

        // Save the download URL to Firebase Realtime Database
        val key = databaseReference.child("AlbumList/${albumCode}").push().key
        key?.let {
            getExifInfo(context, downloadUrl)
            databaseReference.child("AlbumList/${albumCode}").child(it).setValue(downloadUrl.toString())
        }

        downloadUrl.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

//앨범리스트
@Composable
fun AlbumList(albums: List<Album>, albumViewModel: AlbumViewModel) {
    LazyColumn {
        items(albums) { album ->
            Text(album.name, modifier = Modifier.padding(8.dp))
            LazyRow {
                items(album.pictures) { picture ->
                    PictureCard(picture, album.code, albumViewModel)
                }
            }
        }
    }
}

@Composable
fun PictureCard(picture: Picture, albumCode: String, albumViewModel: AlbumViewModel) {
    val likeCount = remember { mutableStateOf(picture.LikeCount) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Date: ${picture.Date}")
            Text("Model: ${picture.Model}")
            Text("Latitude: ${picture.Latitude}")
            Text("Longitude: ${picture.Longitude}")
            Text("Location Tag: ${picture.LocationTag}")
            Text("Likes: ${likeCount.value}")
            Button(onClick = {
                likeCount.value++
                picture.LikeCount = likeCount.value
                val pictureId : String = picture.Date + picture.Model + picture.Latitude + picture.Longitude
                albumViewModel.likePicture(albumCode, pictureId, likeCount.value)  // Assuming `picture.date` as pictureId
            }) {
                Text("Like")
            }
        }
    }
}
