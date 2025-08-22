/*
 * HomeServer - Home server media player website
 *
 * Copyright (C) 2025 NobleNomadic
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
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

// Function to load movie list from the server
function loadMovieList() {
  fetch('/media/')
    .then(response => {
      if (!response.ok) throw new Error("Failed to load movie list");
      return response.text();
    })
    .then(text => {
      // Split plain text into list of movies
      const movieList = text.trim().split("\n");

      const listContainer = document.querySelector(".movieList");
      listContainer.innerHTML = "<h3>Available Movies:</h3>";

      const ul = document.createElement("ul");

      movieList.forEach(movie => {
        const li = document.createElement("li");
        li.textContent = movie;
        li.style.cursor = "pointer";
        li.addEventListener("click", () => {
          document.getElementById("movieNameInput").value = movie;
        });
        ul.appendChild(li);
      });

      listContainer.appendChild(ul);
    })
    .catch(err => {
      console.error("Error loading movie list:", err);
      document.querySelector(".movieList").innerText = "Failed to load movie list.";
    });
}

// Load movie list when the page is loaded
window.addEventListener("DOMContentLoaded", loadMovieList);
