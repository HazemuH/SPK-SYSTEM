import 'package:equatable/equatable.dart';

import 'criterion.dart';

/// A sort option for the catalog (`SortOption`). `id` null = overall score.
class SortOption extends Equatable {
  const SortOption({required this.id, required this.label});

  factory SortOption.fromJson(Map<String, dynamic> json) {
    return SortOption(id: json['id']?.toString(), label: json['label']?.toString() ?? '');
  }

  final String? id;
  final String label;

  @override
  List<Object?> get props => [id, label];
}

/// A category with its toy count (`CategoryView`).
class CategoryOption extends Equatable {
  const CategoryOption({required this.id, required this.name, required this.count});

  factory CategoryOption.fromJson(Map<String, dynamic> json) {
    return CategoryOption(
      id: json['id']?.toString() ?? '',
      name: json['name']?.toString() ?? '',
      count: (json['count'] as num?)?.toInt() ?? 0,
    );
  }

  final String id;
  final String name;
  final int count;

  @override
  List<Object?> get props => [id, name, count];
}

/// Reference data for the mobile app (`Meta`): categories, criteria, sort options.
class Meta extends Equatable {
  const Meta({required this.categories, required this.criteria, required this.sortOptions});

  factory Meta.fromJson(Map<String, dynamic> json) {
    List<T> parse<T>(String key, T Function(Map<String, dynamic>) f) =>
        (json[key] as List?)?.map((e) => f(e as Map<String, dynamic>)).toList() ?? const [];
    return Meta(
      categories: parse('categories', CategoryOption.fromJson),
      criteria: parse('criteria', Criterion.fromJson),
      sortOptions: parse('sortOptions', SortOption.fromJson),
    );
  }

  final List<CategoryOption> categories;
  final List<Criterion> criteria;
  final List<SortOption> sortOptions;

  @override
  List<Object?> get props => [categories, criteria, sortOptions];
}
