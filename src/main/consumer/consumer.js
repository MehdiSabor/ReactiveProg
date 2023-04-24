// Base URL of the server
const baseURL = "http://localhost:8080/";

// Browse a remote shared folder and its sub-folders
function browseFolder(subFolder) {
    const url = new URL("browse", baseURL);
    if (subFolder) {
        url.searchParams.append("subFolder", subFolder);
    }

    return fetch(url)
        .then(response => response.json())
        .catch(error => console.error("Error while browsing folder:", error));
}

// Rename a remote shared file or sub-folder
function renameFile(oldPath, newPath) {
    const url = new URL("rename", baseURL);
    url.searchParams.append("oldpath", oldPath);
    url.searchParams.append("newpath", newPath);

    return fetch(url, { method: "PUT" })
        .then(response => response.text())
        .catch(error => console.error("Error while renaming file:", error));
}

// Download a remote shared file
function downloadFile(path) {
    const url = new URL("download", baseURL);
    url.searchParams.append("path", path);

    return fetch(url)
        .then(response => response.blob())
        .catch(error => console.error("Error while downloading file:", error));
}

// Upload a local file to the remote shared folder
function uploadFile(localPath, sharedPath) {
    const url = new URL("upload", baseURL);
    url.searchParams.append("localpath", localPath);

    if (sharedPath) {
        url.searchParams.append("sharedpath", sharedPath);
    }

    return fetch(url, { method: "POST" })
        .then(response => response.text())
        .catch(error => console.error("Error while uploading file:", error));
}

// Delete a remote shared file
function deleteFile(path) {
    const url = new URL("delete", baseURL);
    url.searchParams.append("path", path);

    return fetch(url, { method: "DELETE" })
        .catch(error => console.error("Error while deleting file:", error));
}


const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

function printMenu() {
  console.log("Please choose an action:");
  console.log("1. Browse shared folder");
  console.log("2. Rename file");
  console.log("3. Download file");
  console.log("4. Upload file");
  console.log("5. Delete file");
  console.log("0. Exit");
}

function main() {
  printMenu();

  rl.question("Enter your choice: ", async (choice) => {
    switch (parseInt(choice)) {
      case 1:
        rl.question("Enter subfolder (leave empty for root): ", async (subFolder) => {
          const files = await browseFolder(subFolder);
          console.log("Files and folders:", files);
          main();
        });
        break;
      case 2:
        rl.question("Enter old file path: ", (oldPath) => {
          rl.question("Enter new file path: ", async (newPath) => {
            const result = await renameFile(oldPath, newPath);
            console.log(result);
            main();
          });
        });
        break;
      case 3:
        rl.question("Enter file path to download: ", async (path) => {
          const blob = await downloadFile(path);
          console.log(`Downloaded file with size ${blob.size} bytes`);
          main();
        });
        break;
      case 4:
        rl.question("Enter local file path: ", (localPath) => {
          rl.question("Enter shared folder path (leave empty for root): ", async (sharedPath) => {
            const result = await uploadFile(localPath, sharedPath);
            console.log(result);
            main();
          });
        });
        break;
      case 5:
        rl.question("Enter file path to delete: ", async (path) => {
          await deleteFile(path);
          console.log("File deleted");
          main();
        });
        break;
      case 0:
        console.log("Goodbye!");
        rl.close();
        break;
      default:
        console.log("Invalid choice");
        main();
    }
  });
}

main();
