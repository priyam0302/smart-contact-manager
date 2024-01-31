console.log("This is the javaScript file");

const toggleSidebar = () => {
	if ($(".sidebar").is(":visible")) {
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
	}
	else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
};

const search = () => {
	let query = $("#search-input").val();

	if (query == '') {
		$(".search-result").hide();
	}
	else {
		console.log(query);

		/*The fetch function is used to fetch a response based on a url*/
		let url = `http://localhost:8282/search/${query}`;
		fetch(url).then((response) => {
			return response.json();
		}).then((data) => {
			/*We are sending html data to the div*/
			console.log(data);

			let text = `<div class='list-group'>`;
			data.forEach((contact) => {
				text += `<a href='/user/contact/${contact.cId}' class='list-group-item list-group-item-action'> ${contact.name} </a>`;
			});
			text += `</div>`;
			$(".search-result").html(text);
			$(".search-result").show();
		});

	}
};