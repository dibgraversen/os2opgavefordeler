(function() {
	'use strict';

	angular.module('topicRouter').factory("flash", flash);

	flash.$inject = [];

	function flash() {
		var messages = [];

		function addMessage(message){
			messages.push(message);
		}

		function deleteMessage(message){
			_.remove(messages, {
				text: message.text
			});
		}

		return {
			messages: messages,
			addMessage: addMessage,
			deleteMessage: deleteMessage
		};
	}
})();
