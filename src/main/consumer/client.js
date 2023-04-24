const baseURL = "http://localhost:8080/";

const RxHttpRequest = rhr.RxHR;

function browseFolder(subFolder) {
    const url = new URL("browse", baseURL);
    if (subFolder) {
        url.searchParams.append("subFolder", subFolder);
    }

    return RxHttpRequest.get(url.toString()).pipe(
        rxjs.operators.map(response => JSON.parse(response.body)),
        rxjs.operators.catchError(error => {
            console.error("Error while browsing folder:", error);
            return rxjs.EMPTY;
        })
    );
}


function displayFolderContent(content) {
    const contentDisplay = document.getElementById("contentDisplay");
    contentDisplay.innerHTML = "";

    content.forEach(item => {
        const div = document.createElement("div");
        div.innerText = item.type + ": " + item.name;
        contentDisplay.appendChild(div);
    });
}

const intervalInput = document.getElementById("interval");
const toggleUpdatesButton = document.getElementById("toggleUpdates");
let intervalSubscription;

toggleUpdatesButton.addEventListener("click", () => {
    if (intervalSubscription) {
        intervalSubscription.unsubscribe();
        intervalSubscription = null;
        toggleUpdatesButton.textContent = "Start";
    } else {
        const intervalValue = parseInt(intervalInput.value);
        if (isNaN(intervalValue) || intervalValue <= 0) {
            alert("Please enter a valid interval value in seconds.");
            return;
        }

        intervalSubscription = rxjs.interval(intervalValue * 1000).pipe(
            rxjs.operators.startWith(0),
            rxjs.operators.switchMap(() => browseFolder(null))
        ).subscribe(content => {
            displayFolderContent(content);
        });

        toggleUpdatesButton.textContent = "Stop";
    }
});
