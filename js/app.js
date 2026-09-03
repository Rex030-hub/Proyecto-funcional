document.addEventListener('DOMContentLoaded', () => {
  const navigation = new NavigationController();
  navigation.init();

  const book = new Book3D(navigation);
  book.init();
});