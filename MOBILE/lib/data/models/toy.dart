import 'package:equatable/equatable.dart';

/// A toy (mainan) as returned by the public API (`ToyView`).
class Toy extends Equatable {
  const Toy({
    required this.id,
    required this.name,
    required this.categoryId,
    required this.category,
    required this.price,
    required this.ageMin,
    required this.ageMax,
    required this.stock,
    required this.tags,
  });

  factory Toy.fromJson(Map<String, dynamic> json) {
    return Toy(
      id: json['id']?.toString() ?? '',
      name: json['name']?.toString() ?? '',
      categoryId: json['categoryId']?.toString() ?? '',
      category: json['category']?.toString() ?? '',
      price: (json['price'] as num?)?.toDouble() ?? 0,
      ageMin: (json['ageMin'] as num?)?.toInt() ?? 0,
      ageMax: (json['ageMax'] as num?)?.toInt() ?? 0,
      stock: (json['stock'] as num?)?.toInt() ?? 0,
      tags: (json['tags'] as List?)?.map((e) => e.toString()).toList() ?? const [],
    );
  }

  final String id;
  final String name;
  final String categoryId;
  final String category;
  final double price;
  final int ageMin;
  final int ageMax;
  final int stock;
  final List<String> tags;

  bool get inStock => stock > 0;

  @override
  List<Object?> get props => [id, name, categoryId, category, price, ageMin, ageMax, stock, tags];
}
