
(function( $ ) {
	$.fn.aoTabs = function(options) {

		var settings = $.extend({
				selectedTab       : 1,
				compressTabs      : false,	// allows for the tabs section to contain a large number of items, out of which all the non-selected tabs are compressed (accordion-like)
				minTabWidth       : 70,		// set a minimum tab width for when the tabs are compressed horizontally
				maxTabWidth       : 230,
				tabAnimationSpeed : 60
			}, options);


		return this.each(function(){

			var wrapper              = $(this),
				wrapperWidth         = wrapper.width(),
				tabsList             = wrapper.children("ul").first().addClass("clearfix"),		// the wrapper that holds the tabs and tabbed content
				tabs                 = tabsList.children("li"),									// the list of tabs
				tabsCount            = tabs.length,
				sections             = wrapper.children(".ao-tabs-section"),					// array with the tabbed content sections
				tabCover             = $("<div />").addClass("ao-whiteCover"),
				tabsViewport         = $("<div />").addClass("ao-tabsViewport"),
				tabsListWidth        = 0;


			/**
			 * Expands the clicked tab in width and compresses all others that are open
			 * @param  {jQuery object} tab The tab that was clicked, passed as a jQuery object
			 * @return {jquery object} The tab that was clicked
			 */
			function expandTab(tab){
				var innerWidth   = tab.attr("data-innerwidth"),
					expandedTabs = tabs.filter(".expanded");

				// Compress all other tabs except for the selected tab
				expandedTabs.addClass("compressed").removeClass("expanded").animate({ "width" : settings.minTabWidth }, settings.tabAnimationSpeed);

				// Expand the selected tab
				tab.removeClass("compressed").addClass("expanded").animate({ "width" : innerWidth }, settings.tabAnimationSpeed);

				return tab;
			}


			/**
			 * Swaps the currently open section with another when a tab is clicked
			 * @return {jQuery} The tab that was clicked
			 */
			function swapTabs()
			{
				var tab = $(this),
					order = parseInt(tab.find("a").first().attr("data-order"), 10);		// return the order number of the section associated with the tab that was clicked

				// Highlight the tab that was clicked
				tabs.removeClass("ao-tab-selected");
				tab.addClass("ao-tab-selected");

				// Move the white tab cover from the previously selected tab to this one
				tabCover.detach().appendTo(tab);

				// Display the content of the section associated with the clicked tab
				sections.removeClass("ao-tabs-section-selected");
				sections.eq(order-1).addClass("ao-tabs-section-selected");

				// When the tabs compression is active, expand the selected tab and compress all others that are not compressed.
				if (settings.compressTabs && tab.hasClass("compressed")) {
					expandTab(tab);
				}
			}



			// Apply custom styling, if any
			if (settings.fontSize) {
				tabs.find("a").css("font-size", settings.fontSize);
			}

			if (settings.maxTabWidth) {
				tabs.css("max-width", settings.maxTabWidth);
			}

			// Create the tabs list viewport, insert the tabs into it and then append the viewport to the "ao-tabs" container
			tabsList.detach();
			tabsViewport.append(tabsList).prependTo(wrapper);


			// Wrap each tab's content within a "listItemInnerWrapper" element and additionally,
			// compute the inner width (excluding the border) of the tabs once they are all rendered and save the values as attributes
			tabs.each(function(){
				var tab                  = $(this),
					innerWidth           = tab.innerWidth(),
					outerWidth           = tab.outerWidth(false),
					content              = tab.html(),
					listItemInnerWrapper = $("<div />").addClass("ao-listItemInnerWrapper"),
					tabGradient          = $("<div />").addClass("ao-tabGradient");

				tab.attr("data-innerwidth", innerWidth)
					.append(tabGradient)
					.children()
					.detach()
					.appendTo(listItemInnerWrapper);

				listItemInnerWrapper.appendTo(tab);

				tabsListWidth += outerWidth;
			});


			// Un-hide the selected tabbed section and highlight its associated tab
			tabs.eq(settings.selectedTab-1).addClass("ao-tab-selected expanded").append(tabCover);
			sections.eq(settings.selectedTab-1).addClass("ao-tabs-section-selected");


			// When the length of the tabs list exceeds the wrapper width (i.e. the tabs overflow their wrapper width), compress the non-selected tabs
			if (tabsListWidth > wrapperWidth) {
				var selectedTabWidth = settings.maxTabWidth + 6,	// 6 = 2 side borders x 3 pixels each
					unselectedTabsBorderWidth = tabsCount * 1;		// 1 px side border for each unselected tab in the list

				// Compute the width of a compressed tab so that they fill up 100% of the available space together
				settings.minTabWidth = Math.floor((wrapperWidth - selectedTabWidth - unselectedTabsBorderWidth) / (tabsCount - 1));

				// When the tabs are compressed to a fixed size each, swapping the tabs may cause the width of entire tabs block 
				// to vary, due to the fact that the original (uncompressed) inner width of some of them may be less than the 
				// maximmum allowed tab width. Hence, we need to force a fixed width for the uncompressed state of all the tabs.
				tabs.attr("data-innerwidth", settings.maxTabWidth);

				if (settings.compressTabs) {
					tabs.not(".ao-tab-selected").css({ "width":settings.minTabWidth }).addClass("compressed");
				}
			}

			// Prevent default `click` action on the tab anchors (e.g. <li><a>tab 1</a></li>)
			tabs.children("a").on("click", function(event){
				event.preventDefault();
			});


			// Listen to the `click` event on the tabs and 
			tabs.on("click", swapTabs);

		});
	};
})( jQuery );