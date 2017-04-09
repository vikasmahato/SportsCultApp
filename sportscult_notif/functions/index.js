var functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.newMatchNotif = functions.database.ref('/sportscult-football-league/Live Matches/{Match}')
    .onWrite(event => {
        const matchId = event.params.Match;
        const match = event.data.val();
        const topic = "news";
        const payload = {
            notification: {
                title: "New" + match["Age Group"] + " match",
                body : match["Team A"] + " vs " + match["Team B"]
            }
        }
        admin.messaging().sendToTopic(topic, payload)
            .then(function(response){
                console.log("New Match notification sent");
            })
    });

exports.matchUpdateNotif = functions.database.ref('/sportscult-football-league/Live Matches/{Match}/{Scored}')
    .onWrite(event => {
        var team;
        var score;
        admin.database().ref('/sportscult-football-league/Live Matches/' + event.params.Match).once('value', function(snapshot){
            if(event.params.Scored == "Team A Goals"){
                team = snapshot.val()["Team A"]
            } else if (event.params.Scored == "Team B Goals"){
                team = snapshot.val()["Team B"]
            } else {
                return;
            }
            const match = snapshot.val();
            const topic = "news";
            const payload = {
            notification: {
                title: match["Age Group"] + match["Team A"] + " vs " + match["Team B"],
                body : team + " scored"
                }
            }
            admin.messaging().sendToTopic(topic, payload)
                .then(function(response){
                    console.log("New GOal scored notification sent");
                })
        });        
    });
