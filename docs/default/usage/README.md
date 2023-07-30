---
sidebar_position: 3
title: Usage
description: How to use Rtag
---

Rtag primarly is used with `RtagEditor` that is extended to edit items, entities and blocks.

```mdx-code-block
import DocCardList from '@theme/DocCardList';
import {useCurrentSidebarCategory} from '@docusaurus/theme-common';

<DocCardList items={useCurrentSidebarCategory().items}/>
```