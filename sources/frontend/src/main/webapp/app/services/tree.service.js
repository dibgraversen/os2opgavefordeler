(function () {
	'use strict';

	angular.module('topicRouter').factory('treeService', treeService);

	treeService.$inject = ['$http', '$q', '$timeout', '$cacheFactory', 'serverUrl', 'appSpinner', '$log'];

	function treeService($http, $q, $timeout, $cacheFactory, serverUrl, appSpinner, $log) {		
		var service = {
			getOuFromTree : getOuFromTree,
			asList : asList,
			remove : remove
		};

		return service;

		function getOuFromTree(ou, tree){
			var flatOuList = [];

			_.each(tree, function(item){
				flatOuList = _.union(flatten(item,flatOuList));
			});

			var result = _.find(flatOuList, function(anOu){
				return anOu.id===ou.id;
			});
			return result;
		}

		function asList(tree){
			var flat = [];
			_.each(tree, function(item){
				flat = _.union(flatten(item,flat));
			});
			return flat; 
		}

		function flatten(node,list){
			list.push(node);

			if (node.children !==null) {
				_.each(node.children, function(item){
						flatten(item,list);
				});
			}
			return list;
		}

		function remove(item,list){
			for (var i=0; i<list.length; i++){
				if(list[i] == item) {
					list.splice(i,1);
					break;
				}
			}
		}		
	}
})();


