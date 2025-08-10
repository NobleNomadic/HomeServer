document.getElementById("triggerMovieLoad").addEventListener("click", function () {
  const movieName = document.getElementById("movieNameInput").value.trim();
  const container = document.getElementById("movieContainer");

  // Clear previous video
  container.innerHTML = "";

  if (movieName === "") {
    container.innerText = "Please enter a movie name.";
    return;
  }

  const videoPath = `/media/${movieName}`;

  // Create video element
  const video = document.createElement("video");
  video.src = videoPath;
  video.controls = true;
  video.autoplay = true;
  video.style.width = "100%";

  // Append video to container
  container.appendChild(video);
});

