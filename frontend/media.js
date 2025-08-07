let movieContainer = document.getElementById("movieContainer");
let movieNameTextInput = document.getElementById("movieNameInput");
let movieLoadButton = document.getElementById("triggerMovieLoad");

function loadMovie() {
  // Get value from input field
  let movieName = movieNameTextInput.value;

  // Create a valid <video> element with the provided source
  movieContainer.innerHTML = `
    <video class="movie" controls autoplay width="800">
      <source src="media/${movieName}.mp4" type="video/mp4">
      Your browser does not support the video tag.
    </video>
  `;
}

// Fix how the event handler is assigned
movieLoadButton.onclick = loadMovie;
