import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.igricaslagalica.R
import com.example.igricaslagalica.model.Friend
import com.example.igricaslagalica.model.Player
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class FriendsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val playersCollection = firestore.collection("players")
    private val user = FirebaseAuth.getInstance().currentUser
    var friendsListStatic = listOf(
        Friend(
            id = "1",
            username = "John",
            profileImage = R.drawable.ic_profile_photo,
            monthlyRank = "Gold",
            stars = 150,
            isActive = true,
            isInGame = false
        ),
        Friend(
            id = "2",
            username = "Emily",
            profileImage = R.drawable.ic_profile_photo,
            monthlyRank = "Silver",
            stars = 100,
            isActive = true,
            isInGame = false
        ),
        Friend(
            id = "3",
            username = "Mike",
            profileImage = R.drawable.ic_profile_photo,
            monthlyRank = "Bronze",
            stars = 75,
            isActive = false,
            isInGame = false
        ),
        Friend(
            id = "4",
            username = "Sarah",
            profileImage = R.drawable.ic_profile_photo,
            monthlyRank = "Gold",
            stars = 200,
            isActive = true,
            isInGame = true
        ),
        Friend(
            id = "5",
            username = "David",
            profileImage = R.drawable.ic_profile_photo,
            monthlyRank = "Silver",
            stars = 120,
            isActive = true,
            isInGame = false
        )
    )

    private val _friendsList = MutableLiveData<List<Friend>>()
    val friendsList: LiveData<List<Friend>>  get() = _friendsList

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error


    fun fetchFriends() {
        playersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val friends = mutableListOf<Friend>()
                for (document in querySnapshot.documents) {
                    val player = document.toObject(Player::class.java)
                    player?.let {
                        player ->
                        if(player.id.equals(user?.uid)) {
                            val currentList = _friendsList.value?.toMutableList() ?: mutableListOf()
                            for (friend in player.friends) {
                                currentList.add(vratiUseraZaID(friend))
                            }
                            _friendsList.value = currentList
                        }
                    }
                }
                _friendsList.value = friends
            }
            .addOnFailureListener { exception ->
                _error.value = exception.message
            }
    }

    private fun vratiUseraZaID(userID: String): Friend {
        TODO(
        )
    }

    fun addFriend(friend: String) {

//        friendsCollection.document(friend.id).set(friend)
//            .addOnSuccessListener {
//                fetchFriends() // Ponovno učitavanje liste prijatelja nakon dodavanja novog prijatelja
//            }
//            .addOnFailureListener { exception ->
//                _error.value = exception.message
//            }
    }
}
