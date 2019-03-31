const functions = require('firebase-functions');

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
		