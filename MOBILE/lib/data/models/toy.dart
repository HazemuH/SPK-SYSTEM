import 'package:equatable/equatable.dart';

/// A toy (mainan) shown to the end user / shop staff — typically an entry in
/// the SPK recommendation ranking. `score` is the computed SPK score; the rank
/// is the item's position in the ordered list.
class Toy extends Equatable {
  const Toy({
    required this.id,
    required this.name,
    required this.category,
    required this.price,
    required this.score,
  });

  factory Toy.fromJson(Map<String, dynamic> json) {
    return Toy(
      id: json['id']?.toString() ?? '',
      name: json['name']?.toString() ?? '',
      category: json['category']?.toString() ?? '',
      price: (json['price'] as num?)?.toDouble() ?? 0,
      score: (json['score'] as num?)?.toDouble() ?? 0,
    );
  }

  final String id;
  final String name;
  final String category;
  final double price;
  final double score;

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'category': category,
        'price': price,
        'score': score,
      };

  @override
  List<Object?> get props => [id, name, category, price, score];
}
