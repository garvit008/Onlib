const functions = require('firebase-functions');
let admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

const request = require('request-promise');
exports.indexRefinedBooksToElastic = functions.database.ref('/refined_books/{book_id}')
	.onWrite((change,context) => {
		let bookData = change.after.val();
		let book_id = context.params.book_id;
		
		console.log('Indexing books: ', bookData);
		
		let elasticSearchCongig = functions.config().elasticsearch;
		let elasticSearchUrl = elasticSearchCongig.url + 'refined_books/refined_books/' + book_id;
		let elasticSearchMethod = bookData ? 'POST' : 'DELETE';
		
		let elasticSearchRequest = {
			method: elasticSearchMethod,
			url: elasticSearchUrl,
			auth:{
				username: elasticSearchCongig.username,
				password: elasticSearchCongig.password,
			},
			body: bookData,
			json: true
		  };
		  
		  return request(elasticSearchRequest).then(response => {
		    return console.log("ElasticSearch response", response);
		  });
     });
	 
	 
	exports.sendNotification = functions.database.ref('/notifications/{userId}').onWrite((change,context) => {
	
	   	    const receiverId = context.params.userId;
	        console.log("receiverId: ", receiverId);
			
			//get the cotent
	        const content = change.after.child('content').val();
	        console.log("message: ", content);
	
	       //get the message date. We'll be sending this in the payload
	       const date = change.after.child('date').val();
	       console.log("date: ", date);
		   
		   const type = change.after.child('type').val();
		   console.log("type: ", type);
			//get the token of the user receiving the message
		   return admin.database().ref("/users/" + receiverId).once('value').then(snap => {
				
				const token = snap.child("messaging_token").val();
				console.log("token: ", token);
				
				//we have everything we need
				//Build the message payload and send the message
				console.log("Construction the notification message.");
				const payload = {
					data: {
						data_type: "book_event",
						title: "New Notification from OnLib",
						data_content: content,
						data_date: date,
						type: type,
					}
				};
				
				return admin.messaging().sendToDevice(token, payload)
							.then(function(response) {
								return console.log("Successfully sent message:", response);
							  })
							  .catch(function(error) {
								return console.log("Error sending message:", error);
							  });
			});
	});
