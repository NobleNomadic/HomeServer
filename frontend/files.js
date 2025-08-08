document.addEventListener("DOMContentLoaded", function () {
  const uploadInput = document.getElementById("uploadFileButton");

  uploadInput.addEventListener("change", async function () {
    if (uploadInput.files.length === 0) {
      alert("No file selected.");
      return;
    }

    const file = uploadInput.files[0];
    const fileName = encodeURIComponent(file.name);
    const uploadUrl = `http://localhost:5400/upload?file=${fileName}`; // Adjust port as needed

    try {
      const response = await fetch(uploadUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/octet-stream",
          "Content-Length": file.size,
        },
        body: file,
      });

      if (response.ok) {
        const message = await response.text();
        alert("[+] Upload successful: " + message);
      } else {
        const error = await response.text();
        alert(`[-] Upload failed: ${response.status} ${response.statusText}\n${error}`);
      }
    } catch (error) {
      console.error("Upload error:", error);
      alert("[-] Error uploading file: " + error.message);
    }
  });
});
