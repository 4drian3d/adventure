/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2023 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.adventure.text.minimessage.tag.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

/**
 * Insert a translation component into the result, with a fallback string.
 *
 * @since 4.13.0
 * @sinceMinecraft 1.19.4
 */
final class TranslatableFallbackTag {
  private static final String TR_OR = "tr_or";
  private static final String TRANSLATE_OR = "translate_or";
  private static final String LANG_OR = "lang_or";

  static final TagResolver RESOLVER = SerializableResolver.claimingComponent(
    StandardTags.names(LANG_OR, TRANSLATE_OR, TR_OR),
    TranslatableFallbackTag::create,
    TranslatableFallbackTag::claim
  );

  private TranslatableFallbackTag() {
  }

  static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
    final String key = args.popOr("A translation key is required").value();
    final String fallback = args.popOr("A fallback messages is required").value();
    final List<Component> with;
    if (args.hasNext()) {
      with = new ArrayList<>();
      while (args.hasNext()) {
        with.add(ctx.deserialize(args.pop().value()));
      }
    } else {
      with = Collections.emptyList();
    }

    return Tag.inserting(Component.translatable(key, fallback, with));
  }

  static @Nullable Emitable claim(final Component input) {
    if (!(input instanceof TranslatableComponent) || ((TranslatableComponent) input).fallback() == null) return null;

    final TranslatableComponent tr = (TranslatableComponent) input;
    return emit -> {
      emit.tag(LANG_OR);
      emit.argument(tr.key());
      emit.argument(tr.fallback());
      for (final Component with : tr.args()) {
        emit.argument(with);
      }
    };
  }
}
